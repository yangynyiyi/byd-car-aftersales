package com.byd.aftersales.service;

import com.byd.aftersales.client.AiAgentClient;
import com.byd.aftersales.common.CacheService;
import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.AgentConversationDao;
import com.byd.aftersales.dao.AgentMessageDao;
import com.byd.aftersales.dao.AgentDiagnosisDao;
import com.byd.aftersales.dao.BatteryHealthDao;
import com.byd.aftersales.dao.FaultRecordDao;
import com.byd.aftersales.dao.VehicleDao;
import com.byd.aftersales.dao.WorkOrderDao;
import com.byd.aftersales.domain.AgentConversation;
import com.byd.aftersales.domain.AgentMessage;
import com.byd.aftersales.domain.BatteryHealthRecord;
import com.byd.aftersales.domain.FaultRecord;
import com.byd.aftersales.domain.Vehicle;
import com.byd.aftersales.domain.WorkOrder;
import com.byd.aftersales.dto.ChatRequest;
import com.byd.aftersales.dto.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private static final String SYSTEM_PROMPT =
            "你是比亚迪新能源汽车售后智能诊断助手。\n" +
            "你可以根据车辆信息、故障描述、电池数据和维修历史进行分析，回答技师的追问。\n" +
            "回答要专业、简洁，控制在200字以内。\n" +
            "如果信息不足，如实告知需要更多数据。";

    private final AgentConversationDao conversationDao;
    private final AgentMessageDao messageDao;
    private final FaultRecordDao faultRecordDao;
    private final VehicleDao vehicleDao;
    private final BatteryHealthDao batteryHealthDao;
    private final WorkOrderDao workOrderDao;
    private final AiAgentClient aiAgentClient;
    private final CacheService cacheService;

    public ConversationService(AgentConversationDao conversationDao,
                               AgentMessageDao messageDao,
                               FaultRecordDao faultRecordDao,
                               VehicleDao vehicleDao,
                               BatteryHealthDao batteryHealthDao,
                               WorkOrderDao workOrderDao,
                               AiAgentClient aiAgentClient,
                               CacheService cacheService) {
        this.conversationDao = conversationDao;
        this.messageDao = messageDao;
        this.faultRecordDao = faultRecordDao;
        this.vehicleDao = vehicleDao;
        this.batteryHealthDao = batteryHealthDao;
        this.workOrderDao = workOrderDao;
        this.aiAgentClient = aiAgentClient;
        this.cacheService = cacheService;
    }

    public AgentConversation createConversation(Long faultId) {
        FaultRecord fault = faultRecordDao.findById(faultId)
                .orElseThrow(() -> new BusinessException("故障记录不存在"));

        Vehicle vehicle = vehicleDao.findByVin(fault.getVin()).orElse(null);
        String title = (vehicle != null ? vehicle.getModel() + " " : "") + fault.getFaultNo();

        AgentConversation conversation = new AgentConversation();
        conversation.setFaultId(faultId);
        conversation.setTitle(title);
        Long id = conversationDao.insert(conversation);
        return conversationDao.findById(id).orElseThrow();
    }

    public ChatResponse chat(ChatRequest request) {
        Long conversationId = request.getConversationId();

        if (conversationId == null) {
            if (request.getFaultId() == null) {
                throw new BusinessException("请提供 conversationId 或 faultId");
            }
            AgentConversation conversation = createConversation(request.getFaultId());
            conversationId = conversation.getConversationId();
        }

        AgentConversation conversation = conversationDao.findById(conversationId)
                .orElseThrow(() -> new BusinessException("会话不存在"));

        AgentMessage userMsg = new AgentMessage();
        userMsg.setConversationId(conversationId);
        userMsg.setRole("user");
        userMsg.setContent(request.getMessage());
        messageDao.insert(userMsg);

        List<AgentMessage> history = messageDao.findByConversationId(conversationId);

        String contextMessage = buildContextMessage(conversation.getFaultId(), history);

        String reply;
        if (aiAgentClient.isConfigured()) {
            try {
                reply = aiAgentClient.call(SYSTEM_PROMPT, contextMessage);
            } catch (Exception e) {
                log.warn("LLM 调用失败，使用本地回复: {}", e.getMessage());
                reply = "抱歉，AI 服务暂时不可用，请稍后重试。";
            }
        } else {
            reply = "AI 服务未配置，请联系管理员。";
        }

        AgentMessage assistantMsg = new AgentMessage();
        assistantMsg.setConversationId(conversationId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(reply);
        messageDao.insert(assistantMsg);

        cacheService.deleteByPrefix("agent:conversation:");

        List<AgentMessage> updatedHistory = messageDao.findByConversationId(conversationId);
        return new ChatResponse(conversationId, reply, updatedHistory);
    }

    public List<AgentMessage> getHistory(Long conversationId) {
        return messageDao.findByConversationId(conversationId);
    }

    public List<AgentConversation> listByFaultId(Long faultId) {
        return conversationDao.findByFaultId(faultId);
    }

    private String buildContextMessage(Long faultId, List<AgentMessage> history) {
        StringBuilder sb = new StringBuilder();

        FaultRecord fault = faultRecordDao.findById(faultId).orElse(null);
        if (fault != null) {
            Vehicle vehicle = vehicleDao.findByVin(fault.getVin()).orElse(null);
            BatteryHealthRecord battery = batteryHealthDao.findLatestByVin(fault.getVin()).orElse(null);
            List<WorkOrder> workOrders = workOrderDao.findRecentByVin(fault.getVin(), 3);

            sb.append("【车辆信息】\n");
            if (vehicle != null) {
                sb.append("- VIN: ").append(vehicle.getVin()).append("\n");
                sb.append("- 车型: ").append(vehicle.getModel()).append("\n");
                sb.append("- 电池型号: ").append(vehicle.getBatteryModel()).append("\n");
                sb.append("- 里程: ").append(vehicle.getCurrentMileage()).append(" km\n");
            }

            sb.append("\n【故障描述】\n");
            sb.append(fault.getFaultDescription()).append("\n");

            if (battery != null) {
                sb.append("\n【电池数据】\n");
                sb.append("- SOH: ").append(battery.getSoh()).append("%\n");
                sb.append("- 充电次数: ").append(battery.getChargeCycles()).append("\n");
                sb.append("- 最高温: ").append(battery.getMaxTemperature()).append("°C\n");
                sb.append("- 压差: ").append(battery.getVoltageDiff()).append("V\n");
            }

            if (workOrders != null && !workOrders.isEmpty()) {
                sb.append("\n【维修记录】\n");
                for (WorkOrder wo : workOrders) {
                    sb.append("- ").append(wo.getWorkOrderNo())
                      .append(" [").append(wo.getStatus()).append("]");
                    if (wo.getRepairResult() != null) {
                        sb.append(" ").append(wo.getRepairResult());
                    }
                    sb.append("\n");
                }
            }
        }

        sb.append("\n【对话历史】\n");
        for (AgentMessage msg : history) {
            String roleLabel = "user".equals(msg.getRole()) ? "技师" : "助手";
            sb.append(roleLabel).append(": ").append(msg.getContent()).append("\n");
        }

        return sb.toString();
    }
}
