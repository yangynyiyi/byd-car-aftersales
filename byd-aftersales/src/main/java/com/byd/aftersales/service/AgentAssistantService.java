package com.byd.aftersales.service;

import com.byd.aftersales.client.AiAgentClient;
import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.AgentDiagnosisDao;
import com.byd.aftersales.dao.BatteryHealthDao;
import com.byd.aftersales.dao.FaultRecordDao;
import com.byd.aftersales.dao.PartDao;
import com.byd.aftersales.dao.PartUsageDao;
import com.byd.aftersales.dao.VehicleDao;
import com.byd.aftersales.dao.WorkOrderDao;
import com.byd.aftersales.domain.AgentDiagnosis;
import com.byd.aftersales.domain.BatteryHealthRecord;
import com.byd.aftersales.domain.FaultRecord;
import com.byd.aftersales.domain.Part;
import com.byd.aftersales.domain.PartUsage;
import com.byd.aftersales.domain.Vehicle;
import com.byd.aftersales.domain.WorkOrder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AgentAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AgentAssistantService.class);

    private static final String SCRIPT_SYSTEM_PROMPT =
            "你是一位经验丰富的比亚迪售后服务顾问。" +
            "请根据诊断结果，用通俗易懂的中文向车主解释故障情况。" +
            "要求：1) 避免使用专业术语，用生活化语言；2) 说明故障原因和影响；3) 给出维修建议和注意事项；4) 语气亲切专业。" +
            "请严格以JSON格式返回：{\"explanation\":\"故障解释\",\"suggestion\":\"维修建议\",\"notes\":\"注意事项\"}。" +
            "只返回纯JSON，不要其他内容。";

    private static final String QUOTE_SYSTEM_PROMPT =
            "你是一位比亚迪售后维修报价专家。" +
            "请根据诊断结果、推荐检测项目和备件价格，生成一份维修报价单。" +
            "要求：1) 全部使用中文；2) 列出可能需要的备件及预估价格；3) 给出工时费估算；4) 给出总价区间。" +
            "请严格以JSON格式返回：{\"items\":[{\"name\":\"项目名称\",\"type\":\"备件/工时\",\"estimatedPrice\":预估价格}],\"laborEstimate\":工时费估算,\"totalMin\":最低总价,\"totalMax\":最高总价,\"notes\":\"报价说明\"}。" +
            "只返回纯JSON，不要其他内容。";

    private static final String MAINTENANCE_SYSTEM_PROMPT =
            "你是一位比亚迪新能源汽车保养专家。" +
            "请根据车辆里程、电池健康度、使用习惯等信息，生成个性化保养计划建议。" +
            "要求：1) 全部使用中文；2) 按紧急程度排序；3) 说明每项保养的原因和建议时间。" +
            "请严格以JSON格式返回：{\"recommendations\":[{\"item\":\"保养项目\",\"priority\":\"紧急/重要/建议\",\"reason\":\"原因\",\"suggestedTime\":\"建议时间\"}],\"summary\":\"整体保养建议\"}。" +
            "只返回纯JSON，不要其他内容。";

    private final AiAgentClient aiAgentClient;
    private final AgentDiagnosisDao agentDiagnosisDao;
    private final FaultRecordDao faultRecordDao;
    private final VehicleDao vehicleDao;
    private final WorkOrderDao workOrderDao;
    private final PartUsageDao partUsageDao;
    private final PartDao partDao;
    private final BatteryHealthDao batteryHealthDao;

    public AgentAssistantService(AiAgentClient aiAgentClient,
                                 AgentDiagnosisDao agentDiagnosisDao,
                                 FaultRecordDao faultRecordDao,
                                 VehicleDao vehicleDao,
                                 WorkOrderDao workOrderDao,
                                 PartUsageDao partUsageDao,
                                 PartDao partDao,
                                 BatteryHealthDao batteryHealthDao) {
        this.aiAgentClient = aiAgentClient;
        this.agentDiagnosisDao = agentDiagnosisDao;
        this.faultRecordDao = faultRecordDao;
        this.vehicleDao = vehicleDao;
        this.workOrderDao = workOrderDao;
        this.partUsageDao = partUsageDao;
        this.partDao = partDao;
        this.batteryHealthDao = batteryHealthDao;
    }

    public String generateCustomerScript(Long diagnosisId) {
        AgentDiagnosis diagnosis = agentDiagnosisDao.findById(diagnosisId)
                .orElseThrow(() -> new BusinessException("诊断记录不存在"));
        FaultRecord fault = faultRecordDao.findById(diagnosis.getFaultId())
                .orElseThrow(() -> new BusinessException("关联故障不存在"));
        Vehicle vehicle = vehicleDao.findByVin(fault.getVin()).orElse(null);

        StringBuilder userMsg = new StringBuilder();
        userMsg.append("【车辆信息】\n");
        if (vehicle != null) {
            userMsg.append("- 车型：").append(vehicle.getModel()).append("\n");
            userMsg.append("- 车牌：").append(vehicle.getLicensePlate()).append("\n");
        }
        userMsg.append("\n【故障描述】\n").append(fault.getFaultDescription()).append("\n");
        userMsg.append("\n【诊断结果】\n");
        userMsg.append("- 诊断建议：").append(diagnosis.getDiagnosisSuggestion()).append("\n");
        userMsg.append("- 风险等级：").append(diagnosis.getRiskLevel()).append("\n");
        userMsg.append("- 推荐检测：").append(diagnosis.getRecommendedChecks()).append("\n");

        return callAgentWithFallback(SCRIPT_SYSTEM_PROMPT, userMsg.toString());
    }

    public String generateRepairQuote(Long diagnosisId) {
        AgentDiagnosis diagnosis = agentDiagnosisDao.findById(diagnosisId)
                .orElseThrow(() -> new BusinessException("诊断记录不存在"));
        FaultRecord fault = faultRecordDao.findById(diagnosis.getFaultId())
                .orElseThrow(() -> new BusinessException("关联故障不存在"));
        Vehicle vehicle = vehicleDao.findByVin(fault.getVin()).orElse(null);

        // 查找关联工单的备件使用情况
        List<WorkOrder> workOrders = workOrderDao.findByFaultId(fault.getFaultId());
        StringBuilder partsInfo = new StringBuilder();
        BigDecimal existingPartCost = BigDecimal.ZERO;
        for (WorkOrder wo : workOrders) {
            List<PartUsage> usages = partUsageDao.findApprovedByWorkOrderId(wo.getWorkOrderId());
            for (PartUsage usage : usages) {
                Part part = partDao.findById(usage.getPartId()).orElse(null);
                String partName = part != null ? part.getPartName() : "未知备件";
                BigDecimal lineCost = usage.getUnitPrice().multiply(BigDecimal.valueOf(usage.getQuantity()));
                partsInfo.append("- ").append(partName)
                        .append(" x").append(usage.getQuantity())
                        .append(" 单价：").append(usage.getUnitPrice())
                        .append(" 小计：").append(lineCost).append("\n");
                existingPartCost = existingPartCost.add(lineCost);
            }
        }

        StringBuilder userMsg = new StringBuilder();
        userMsg.append("【车辆信息】\n");
        if (vehicle != null) {
            userMsg.append("- 车型：").append(vehicle.getModel()).append("\n");
        }
        userMsg.append("\n【故障描述】\n").append(fault.getFaultDescription()).append("\n");
        userMsg.append("\n【诊断结果】\n");
        userMsg.append("- 诊断建议：").append(diagnosis.getDiagnosisSuggestion()).append("\n");
        userMsg.append("- 风险等级：").append(diagnosis.getRiskLevel()).append("\n");
        userMsg.append("- 推荐检测：").append(diagnosis.getRecommendedChecks()).append("\n");
        if (partsInfo.length() > 0) {
            userMsg.append("\n【已审批备件】\n").append(partsInfo);
            userMsg.append("已审批备件费用合计：").append(existingPartCost).append(" 元\n");
        } else {
            userMsg.append("\n【已审批备件】\n暂无已审批备件，请根据推荐检测项目预估所需备件。\n");
        }

        return callAgentWithFallback(QUOTE_SYSTEM_PROMPT, userMsg.toString());
    }

    private String callAgentWithFallback(String systemPrompt, String userMessage) {
        if (!aiAgentClient.isConfigured()) {
            log.warn("未配置 ai.agent.api-key，使用本地模板回复");
            return buildLocalFallback(systemPrompt, userMessage);
        }
        try {
            return aiAgentClient.call(systemPrompt, userMessage);
        } catch (Exception e) {
            log.warn("AI 调用失败，使用本地模板回复: {}", e.getMessage());
            return buildLocalFallback(systemPrompt, userMessage);
        }
    }

    public String generateMaintenancePlan(String vin) {
        Vehicle vehicle = vehicleDao.findByVin(vin)
                .orElseThrow(() -> new BusinessException("车辆不存在"));
        BatteryHealthRecord battery = batteryHealthDao.findLatestByVin(vin).orElse(null);
        List<WorkOrder> history = workOrderDao.findRecentByVin(vin, 5);

        StringBuilder userMsg = new StringBuilder();
        userMsg.append("【车辆信息】\n");
        userMsg.append("- 车型：").append(vehicle.getModel()).append("\n");
        userMsg.append("- 当前里程：").append(vehicle.getCurrentMileage()).append(" km\n");
        userMsg.append("- 购车时间：").append(vehicle.getPurchaseDate()).append("\n");
        if (vehicle.getNextMaintenanceDate() != null) {
            userMsg.append("- 下次保养日期：").append(vehicle.getNextMaintenanceDate()).append("\n");
        }

        userMsg.append("\n【电池健康数据】\n");
        if (battery != null) {
            userMsg.append("- 电池健康度(SOH)：").append(battery.getSoh()).append("%\n");
            userMsg.append("- 累计充电次数：").append(battery.getChargeCycles()).append("\n");
            userMsg.append("- 最高温度：").append(battery.getMaxTemperature()).append("°C\n");
            userMsg.append("- 电芯压差：").append(battery.getVoltageDiff()).append("V\n");
        } else {
            userMsg.append("暂无电池检测数据\n");
        }

        userMsg.append("\n【历史维修记录】\n");
        if (history != null && !history.isEmpty()) {
            for (WorkOrder wo : history) {
                userMsg.append("- 工单号：").append(wo.getWorkOrderNo())
                        .append("，状态：").append(wo.getStatus());
                if (wo.getRepairResult() != null) {
                    userMsg.append("，结果：").append(wo.getRepairResult());
                }
                userMsg.append("\n");
            }
        } else {
            userMsg.append("暂无历史维修记录\n");
        }

        return callAgentWithFallback(MAINTENANCE_SYSTEM_PROMPT, userMsg.toString());
    }

    private String buildLocalFallback(String systemPrompt, String userMessage) {
        if (systemPrompt.contains("售后服务顾问")) {
            return "{\"explanation\":\"根据诊断结果，您的车辆存在需要关注的故障情况，建议尽快安排检修以确保行车安全。\",\"suggestion\":\"建议尽快到比亚迪授权服务中心进行全面检测和维修。\",\"notes\":\"维修期间请勿强行使用车辆，以免造成更大损失。\"}";
        } else if (systemPrompt.contains("保养专家")) {
            return "{\"recommendations\":[{\"item\":\"常规保养检查\",\"priority\":\"建议\",\"reason\":\"定期保养可延长车辆使用寿命\",\"suggestedTime\":\"按保养手册周期\"},{\"item\":\"电池健康检测\",\"priority\":\"重要\",\"reason\":\"确保电池性能和安全\",\"suggestedTime\":\"每半年一次\"}],\"summary\":\"建议按保养手册定期保养，关注电池健康状态。\"}";
        } else {
            return "{\"items\":[{\"name\":\"检测费\",\"type\":\"工时\",\"estimatedPrice\":200},{\"name\":\"备件费（预估）\",\"type\":\"备件\",\"estimatedPrice\":500}],\"laborEstimate\":200,\"totalMin\":500,\"totalMax\":1000,\"notes\":\"以上为预估价格，实际费用以服务中心报价为准。\"}";
        }
    }
}
