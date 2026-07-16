package com.byd.aftersales.service.impl;

import com.byd.aftersales.client.AiAgentClient;
import com.byd.aftersales.common.CacheService;
import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.AgentDiagnosisDao;
import com.byd.aftersales.dao.BatteryHealthDao;
import com.byd.aftersales.dao.FaultRecordDao;
import com.byd.aftersales.dao.VehicleDao;
import com.byd.aftersales.dao.WorkOrderDao;
import com.byd.aftersales.domain.AgentDiagnosis;
import com.byd.aftersales.domain.BatteryHealthRecord;
import com.byd.aftersales.domain.FaultRecord;
import com.byd.aftersales.domain.Vehicle;
import com.byd.aftersales.domain.WorkOrder;
import com.byd.aftersales.dto.AgentDiagnoseRequest;
import com.byd.aftersales.service.AgentDiagnosisService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class AgentDiagnosisServiceImpl implements AgentDiagnosisService {

    private static final Logger log = LoggerFactory.getLogger(AgentDiagnosisServiceImpl.class);

    private static final String SYSTEM_PROMPT =
            "你是一位资深的新能源汽车电池诊断专家。" +
            "请根据提供的车辆信息、故障描述、电池检测数据和历史维修记录进行诊断分析。" +
            "请严格以JSON格式返回结果，包含以下字段：" +
            "diagnosisSuggestion(诊断建议，200字以内，全部使用中文，不要出现英文单词或缩写)、" +
            "riskLevel(LOW/MEDIUM/HIGH，必须与诊断建议的风险程度一致)、" +
            "recommendedChecks(推荐检测项目，用分号分隔，全部使用中文，不要出现英文)。" +
            "只返回纯JSON文本，不要使用markdown代码块包裹，不要返回任何其他内容。";

    private final AgentDiagnosisDao agentDiagnosisDao;
    private final FaultRecordDao faultRecordDao;
    private final VehicleDao vehicleDao;
    private final BatteryHealthDao batteryHealthDao;
    private final WorkOrderDao workOrderDao;
    private final AiAgentClient aiAgentClient;
    private final CacheService cacheService;

    public AgentDiagnosisServiceImpl(AgentDiagnosisDao agentDiagnosisDao,
                                     FaultRecordDao faultRecordDao,
                                     VehicleDao vehicleDao,
                                     BatteryHealthDao batteryHealthDao,
                                     WorkOrderDao workOrderDao,
                                     AiAgentClient aiAgentClient,
                                     CacheService cacheService) {
        this.agentDiagnosisDao = agentDiagnosisDao;
        this.faultRecordDao = faultRecordDao;
        this.vehicleDao = vehicleDao;
        this.batteryHealthDao = batteryHealthDao;
        this.workOrderDao = workOrderDao;
        this.aiAgentClient = aiAgentClient;
        this.cacheService = cacheService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AgentDiagnosis getById(Long diagnosisId) {
        String key = "agent:diagnosis:" + diagnosisId;
        Object cached = cacheService.get(key);
        if (cached instanceof AgentDiagnosis) {
            return (AgentDiagnosis) cached;
        }
        AgentDiagnosis diagnosis = agentDiagnosisDao.findById(diagnosisId).orElse(null);
        if (diagnosis != null) {
            cacheService.put(key, diagnosis, 30);
        }
        return diagnosis;
    }

    @Override
    public List<AgentDiagnosis> listByFaultId(Long faultId) {
        return agentDiagnosisDao.findByFaultId(faultId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AgentDiagnosis> listByVin(String vin) {
        String key = "agent:diagnosis:vin:" + vin;
        Object cached = cacheService.get(key);
        if (cached instanceof List) {
            return (List<AgentDiagnosis>) cached;
        }
        List<AgentDiagnosis> list = agentDiagnosisDao.findByVin(vin);
        cacheService.put(key, list, 10);
        return list;
    }

    @Override
    public AgentDiagnosis diagnose(AgentDiagnoseRequest request) {
        String input = request.getFaultDesc();
        if (input == null || input.isBlank()) {
            throw new BusinessException("故障描述不能为空");
        }

        FaultRecord fault = resolveFault(request);
        Vehicle vehicle = vehicleDao.findByVin(fault.getVin()).orElse(null);
        BatteryHealthRecord battery = batteryHealthDao.findLatestByVin(fault.getVin()).orElse(null);
        List<WorkOrder> history = workOrderDao.findRecentByVin(fault.getVin(), 5);

        DiagnosisResult result = callAgentOrFallback(vehicle, fault, input, battery, history);

        AgentDiagnosis diagnosis = new AgentDiagnosis();
        diagnosis.setFaultId(fault.getFaultId());
        diagnosis.setInputText(input);
        diagnosis.setDiagnosisSuggestion(result.suggestion());
        diagnosis.setRiskLevel(result.riskLevel());
        diagnosis.setRecommendedChecks(result.checks());
        diagnosis.setConfidenceScore(result.confidence());
        diagnosis.setAgentName(result.agentName());
        diagnosis.setRawResponse(result.raw());

        Long id = agentDiagnosisDao.insert(diagnosis);
        faultRecordDao.updateStatus(fault.getFaultNo(), "DIAGNOSED");

        cacheService.deleteByPrefix("agent:diagnosis:");

        return agentDiagnosisDao.findById(id).orElseThrow(() -> new BusinessException("诊断记录保存失败"));
    }

    private FaultRecord resolveFault(AgentDiagnoseRequest request) {
        if (request.getFaultId() != null) {
            return faultRecordDao.findById(request.getFaultId())
                    .orElseThrow(() -> new BusinessException("故障记录不存在"));
        }
        if (request.getVin() == null || request.getVin().isBlank()) {
            throw new BusinessException("请提供 VIN 或故障 ID");
        }
        List<FaultRecord> faults = faultRecordDao.findByVin(request.getVin());
        return faults.stream()
                .filter(f -> !"CLOSED".equals(f.getStatus()) && !"WORK_ORDER_CREATED".equals(f.getStatus()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("该车辆暂无待诊断故障，请先登记故障"));
    }

    private DiagnosisResult callAgentOrFallback(Vehicle vehicle,
                                                FaultRecord fault,
                                                String input,
                                                BatteryHealthRecord battery,
                                                List<WorkOrder> history) {
        if (!aiAgentClient.isConfigured()) {
            log.warn("未配置 ai.agent.api-key，使用本地规则诊断");
            return buildFallbackDiagnosis(input);
        }

        try {
            String userMessage = buildUserMessage(vehicle, fault, input, battery, history);
            String rawResponse = aiAgentClient.call(SYSTEM_PROMPT, userMessage);
            return parseAgentResponse(rawResponse, "GLM-4.6V-Flash");
        } catch (Exception e) {
            log.warn("远程 Agent 调用失败，回退本地规则: {}", e.getMessage());
            return buildFallbackDiagnosis(input);
        }
    }

    private String buildUserMessage(Vehicle vehicle,
                                    FaultRecord fault,
                                    String input,
                                    BatteryHealthRecord battery,
                                    List<WorkOrder> history) {
        StringBuilder sb = new StringBuilder();

        sb.append("【车辆信息】\n");
        if (vehicle != null) {
            sb.append("- VIN: ").append(vehicle.getVin()).append("\n");
            sb.append("- 车型: ").append(vehicle.getModel()).append("\n");
            sb.append("- 电池型号: ").append(vehicle.getBatteryModel()).append("\n");
            sb.append("- 当前里程: ").append(vehicle.getCurrentMileage()).append(" km\n");
            sb.append("- 购车时间: ").append(vehicle.getPurchaseDate()).append("\n");
        } else {
            sb.append("- VIN: ").append(fault.getVin()).append("\n");
        }

        sb.append("\n【故障描述】\n");
        sb.append(input).append("\n");
        if (fault.getFaultLevel() != null) {
            sb.append("- 故障等级: ").append(fault.getFaultLevel()).append("\n");
        }

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

    private DiagnosisResult parseAgentResponse(String rawResponse, String agentName) {
        try {
            String jsonStr = extractJson(rawResponse);
            JSONObject json = new JSONObject(jsonStr);
            return new DiagnosisResult(
                    json.optString("diagnosisSuggestion", ""),
                    json.optString("riskLevel", "LOW"),
                    json.optString("recommendedChecks", ""),
                    json.has("confidenceScore") ? json.getBigDecimal("confidenceScore") : BigDecimal.ZERO,
                    agentName,
                    rawResponse
            );
        } catch (Exception e) {
            log.warn("Agent 返回 JSON 解析失败, 尝试字段提取: {}", e.getMessage());
            return extractFieldsFromRaw(rawResponse, agentName);
        }
    }

    private String extractJson(String raw) {
        String s = raw.trim();
        // 1. 尝试提取 ```json ... ``` 代码块
        if (s.contains("```json")) {
            int start = s.indexOf("```json") + 7;
            int end = s.indexOf("```", start);
            if (end > start) {
                return s.substring(start, end).trim();
            }
        }
        // 2. 尝试提取 ``` ... ``` 代码块
        if (s.contains("```")) {
            int start = s.indexOf("```") + 3;
            int end = s.indexOf("```", start);
            if (end > start) {
                return s.substring(start, end).trim();
            }
        }
        // 3. 提取第一个 { 到最后一个 } 之间的内容
        int firstBrace = s.indexOf('{');
        int lastBrace = s.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return s.substring(firstBrace, lastBrace + 1).trim();
        }
        return s;
    }

    private DiagnosisResult extractFieldsFromRaw(String raw, String agentName) {
        String suggestion = raw;
        String riskLevel = "LOW";
        String checks = "建议人工进一步检查";

        // 尝试从原始文本中提取 diagnosisSuggestion 字段
        String sugPattern = "\"diagnosisSuggestion\"";
        int sugIdx = raw.indexOf(sugPattern);
        if (sugIdx >= 0) {
            int colonIdx = raw.indexOf(':', sugIdx + sugPattern.length());
            if (colonIdx >= 0) {
                int quoteStart = raw.indexOf('"', colonIdx + 1);
                if (quoteStart >= 0) {
                    int quoteEnd = findClosingQuote(raw, quoteStart + 1);
                    if (quoteEnd > quoteStart) {
                        suggestion = raw.substring(quoteStart + 1, quoteEnd);
                    }
                }
            }
        }

        // 尝试从原始文本中提取 riskLevel 字段
        String riskPattern = "\"riskLevel\"";
        int riskIdx = raw.indexOf(riskPattern);
        if (riskIdx >= 0) {
            int colonIdx = raw.indexOf(':', riskIdx + riskPattern.length());
            if (colonIdx >= 0) {
                int quoteStart = raw.indexOf('"', colonIdx + 1);
                if (quoteStart >= 0) {
                    int quoteEnd = raw.indexOf('"', quoteStart + 1);
                    if (quoteEnd > quoteStart) {
                        riskLevel = raw.substring(quoteStart + 1, quoteEnd);
                    }
                }
            }
        }

        // 尝试从原始文本中提取 recommendedChecks 字段
        String checksPattern = "\"recommendedChecks\"";
        int checksIdx = raw.indexOf(checksPattern);
        if (checksIdx >= 0) {
            int colonIdx = raw.indexOf(':', checksIdx + checksPattern.length());
            if (colonIdx >= 0) {
                int quoteStart = raw.indexOf('"', colonIdx + 1);
                if (quoteStart >= 0) {
                    int quoteEnd = findClosingQuote(raw, quoteStart + 1);
                    if (quoteEnd > quoteStart) {
                        checks = raw.substring(quoteStart + 1, quoteEnd);
                    }
                }
            }
        }

        log.info("从原始响应中提取字段: riskLevel={}, checks={}", riskLevel, checks);
        return new DiagnosisResult(suggestion, riskLevel, checks, BigDecimal.ZERO, agentName, raw);
    }

    private int findClosingQuote(String s, int start) {
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                return i;
            }
        }
        return -1;
    }

    private DiagnosisResult buildFallbackDiagnosis(String input) {
        String text = input.toLowerCase(Locale.ROOT);
        if (text.contains("电池") || text.contains("续航") || text.contains("充电")) {
            return new DiagnosisResult(
                    "疑似动力电池或 BMS 相关故障，建议优先检测 SOC/SOH、绝缘阻值与充电回路。",
                    "HIGH",
                    "电池健康检测;绝缘测试;充电口与高压互锁检查",
                    new BigDecimal("86.50"),
                    "BYD-Repair-Agent-Local",
                    "{\"category\":\"BATTERY\",\"priority\":1}"
            );
        }
        if (text.contains("异响") || text.contains("抖动") || text.contains("顿挫")) {
            return new DiagnosisResult(
                    "疑似驱动系统或底盘部件异常，建议检查电机轴承、减速器及悬架连接件。",
                    "MEDIUM",
                    "路试复现;电机轴承听诊;底盘紧固件扭矩检查",
                    new BigDecimal("78.20"),
                    "BYD-Repair-Agent-Local",
                    "{\"category\":\"DRIVETRAIN\",\"priority\":2}"
            );
        }
        if (text.contains("报警") || text.contains("故障灯") || text.contains("无法启动")) {
            return new DiagnosisResult(
                    "疑似整车控制系统告警，建议读取 DTC 并结合高压/低压供电状态排查。",
                    "HIGH",
                    "全车故障码读取;12V 蓄电池检测;高压上电流程检查",
                    new BigDecimal("82.00"),
                    "BYD-Repair-Agent-Local",
                    "{\"category\":\"ECU\",\"priority\":1}"
            );
        }
        return new DiagnosisResult(
                "根据描述暂无法锁定单一故障点，建议先做基础问诊与全车扫描，再按模块逐步排查。",
                "LOW",
                "全车故障码扫描;基础电气检查;客户问诊记录复核",
                new BigDecimal("65.00"),
                "BYD-Repair-Agent-Local",
                "{\"category\":\"GENERAL\",\"priority\":3}"
        );
    }

    private record DiagnosisResult(String suggestion,
                                   String riskLevel,
                                   String checks,
                                   BigDecimal confidence,
                                   String agentName,
                                   String raw) {}
}
