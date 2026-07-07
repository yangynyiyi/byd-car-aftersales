package com.byd.agent.service.impl;

import com.byd.agent.client.AiAgentClient;
import com.byd.agent.dao.AgentDiagnosisDao;
import com.byd.agent.dao.FaultRecordDao;
import com.byd.agent.dao.VehicleDao;
import com.byd.agent.dao.WorkOrderDao;
import com.byd.agent.model.*;
import com.byd.agent.service.AgentDiagnosisService;
import com.byd.battery.dao.BatteryHealthDao;
import com.byd.battery.model.BatteryHealthRecord;
import com.byd.config.CacheService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AgentDiagnosisServiceImpl implements AgentDiagnosisService {

    private static final Logger log = LoggerFactory.getLogger(AgentDiagnosisServiceImpl.class);

    @Autowired private AgentDiagnosisDao agentDiagnosisDao;
    @Autowired private FaultRecordDao faultRecordDao;
    @Autowired private VehicleDao vehicleDao;
    @Autowired private WorkOrderDao workOrderDao;
    @Autowired private BatteryHealthDao batteryHealthDao;
    @Autowired private CacheService cacheService;
    @Autowired private AiAgentClient aiAgentClient;

    private static final String SYSTEM_PROMPT =
            "你是一位资深的新能源汽车电池诊断专家。" +
            "请根据提供的车辆信息、故障描述、电池检测数据和历史维修记录进行诊断分析。" +
            "请严格以JSON格式返回结果，包含以下字段：" +
            "diagnosisSuggestion(诊断建议，200字以内)、" +
            "riskLevel(LOW/MEDIUM/HIGH)、" +
            "recommendedChecks(推荐检测项目，用分号分隔)、" +
            "confidenceScore(置信度，0-1之间的小数)。" +
            "只返回JSON，不要返回其他内容。";

    @Override
    public AgentDiagnosisResult diagnose(Long faultId) {
        // 1. 查询故障记录
        FaultRecord fault = faultRecordDao.selectById(faultId);
        if (fault == null) {
            throw new RuntimeException("故障记录不存在: " + faultId);
        }

        // 2. 查询车辆信息
        Vehicle vehicle = vehicleDao.selectByVin(fault.getVin());

        // 3. 查询最近电池数据
        BatteryHealthRecord battery = batteryHealthDao.selectLatestByVin(fault.getVin());

        // 4. 查询历史维修记录（最近5条）
        List<WorkOrder> history = workOrderDao.selectRecentByVin(fault.getVin(), 5);

        // 5. 组装 Prompt
        String userMessage = buildUserMessage(vehicle, fault, battery, history);
        log.info("组装 Agent 输入完成, faultId={}, vin={}", faultId, fault.getVin());

        // 6. 调用 Agent
        String rawResponse = aiAgentClient.call(SYSTEM_PROMPT, userMessage);

        // 7. 解析结果
        AgentDiagnosisResult result = parseResponse(rawResponse);

        // 8. 保存到数据库
        AgentDiagnosis record = new AgentDiagnosis();
        record.setFaultId(faultId);
        record.setInputText(userMessage);
        record.setDiagnosisSuggestion(result.getDiagnosisSuggestion());
        record.setRiskLevel(result.getRiskLevel());
        record.setRecommendedChecks(result.getRecommendedChecks());
        record.setConfidenceScore(result.getConfidenceScore());
        record.setAgentName("qwen-plus");
        record.setRawResponse(rawResponse);
        agentDiagnosisDao.insert(record);

        log.info("Agent 诊断完成, faultId={}, riskLevel={}", faultId, result.getRiskLevel());
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AgentDiagnosis getById(Long diagnosisId) {
        String key = "agent:diagnosis:" + diagnosisId;
        Object cached = cacheService.get(key);
        if (cached instanceof AgentDiagnosis) {
            return (AgentDiagnosis) cached;
        }
        AgentDiagnosis diagnosis = agentDiagnosisDao.selectById(diagnosisId);
        if (diagnosis != null) {
            cacheService.put(key, diagnosis, 30);
        }
        return diagnosis;
    }

    @Override
    public List<AgentDiagnosis> getByFaultId(Long faultId) {
        return agentDiagnosisDao.selectByFaultId(faultId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AgentDiagnosis> getByVin(String vin) {
        String key = "agent:diagnosis:vin:" + vin;
        Object cached = cacheService.get(key);
        if (cached instanceof List) {
            return (List<AgentDiagnosis>) cached;
        }
        List<AgentDiagnosis> list = agentDiagnosisDao.selectByVin(vin);
        cacheService.put(key, list, 10);
        return list;
    }

    private String buildUserMessage(Vehicle vehicle, FaultRecord fault,
                                     BatteryHealthRecord battery, List<WorkOrder> history) {
        StringBuilder sb = new StringBuilder();

        sb.append("【车辆信息】\n");
        if (vehicle != null) {
            sb.append("- VIN: ").append(vehicle.getVin()).append("\n");
            sb.append("- 车型: ").append(vehicle.getModel()).append("\n");
            sb.append("- 电池型号: ").append(vehicle.getBatteryModel()).append("\n");
            sb.append("- 当前里程: ").append(vehicle.getCurrentMileage()).append(" km\n");
            sb.append("- 购车时间: ").append(vehicle.getPurchaseDate()).append("\n");
        }

        sb.append("\n【故障描述】\n");
        sb.append(fault.getFaultDescription()).append("\n");
        sb.append("- 故障等级: ").append(fault.getFaultLevel()).append("\n");

        sb.append("\n【最近电池检测数据】\n");
        if (battery != null) {
            sb.append("- 电池健康度(SOH): ").append(battery.getSoh()).append("%\n");
            sb.append("- 累计充电次数: ").append(battery.getChargeCycles()).append("\n");
            sb.append("- 最高温度: ").append(battery.getMaxTemperature()).append("°C\n");
            sb.append("- 最低温度: ").append(battery.getMinTemperature()).append("°C\n");
            sb.append("- 电芯压差: ").append(battery.getVoltageDiff()).append("V\n");
        } else {
            sb.append("暂无电池检测数据\n");
        }

        sb.append("\n【历史维修记录】\n");
        if (history != null && !history.isEmpty()) {
            for (WorkOrder wo : history) {
                sb.append("- 工单号: ").append(wo.getWorkOrderNo())
                  .append(", 状态: ").append(wo.getStatus());
                if (wo.getRepairResult() != null) {
                    sb.append(", 结果: ").append(wo.getRepairResult());
                }
                sb.append("\n");
            }
        } else {
            sb.append("暂无历史维修记录\n");
        }

        return sb.toString();
    }

    private AgentDiagnosisResult parseResponse(String rawResponse) {
        AgentDiagnosisResult result = new AgentDiagnosisResult();
        try {
            // 尝试从返回内容中提取 JSON
            String jsonStr = rawResponse.trim();
            // 如果返回内容包含 ```json 包裹，先提取
            if (jsonStr.contains("```json")) {
                int start = jsonStr.indexOf("```json") + 7;
                int end = jsonStr.indexOf("```", start);
                if (end > start) {
                    jsonStr = jsonStr.substring(start, end).trim();
                }
            } else if (jsonStr.contains("```")) {
                int start = jsonStr.indexOf("```") + 3;
                int end = jsonStr.indexOf("```", start);
                if (end > start) {
                    jsonStr = jsonStr.substring(start, end).trim();
                }
            }

            JSONObject json = new JSONObject(jsonStr);
            result.setDiagnosisSuggestion(json.optString("diagnosisSuggestion", ""));
            result.setRiskLevel(json.optString("riskLevel", "LOW"));
            result.setRecommendedChecks(json.optString("recommendedChecks", ""));
            result.setConfidenceScore(json.has("confidenceScore")
                    ? json.getBigDecimal("confidenceScore") : BigDecimal.ZERO);

        } catch (Exception e) {
            log.warn("Agent 返回 JSON 解析失败, 使用原始文本: {}", e.getMessage());
            result.setDiagnosisSuggestion(rawResponse);
            result.setRiskLevel("LOW");
            result.setRecommendedChecks("建议人工进一步检查");
            result.setConfidenceScore(BigDecimal.ZERO);
        }
        return result;
    }
}
