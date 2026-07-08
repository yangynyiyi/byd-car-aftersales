# D 模块接口说明（远程 Agent 诊断 + 电池健康预警）

基础地址：`http://localhost:8080`

## Agent 远程诊断

基于通义千问（qwen-plus）大模型，根据故障描述、车辆信息、电池数据和历史维修记录自动诊断。

- `POST /api/agent-diagnosis/trigger?faultId=` — 触发 AI 诊断（返回诊断建议、风险等级、推荐检测项、置信度）
- `GET /api/agent-diagnosis/trigger?faultId=` — 同上（GET 方便浏览器测试）
- `GET /api/agent-diagnosis/{diagnosisId}` — 按 ID 查诊断记录
- `GET /api/agent-diagnosis/list?vin=` — 按 VIN 查该车所有诊断记录
- `GET /api/agent-diagnosis/by-fault/{faultId}` — 按故障 ID 查诊断记录

### 请求示例

触发诊断（需先有 fault_record 数据）：

```
GET http://localhost:8080/api/agent-diagnosis/trigger?faultId=1
```

### 响应示例

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "diagnosisSuggestion": "SOC跳降可能由BMS均衡异常或电芯老化引起...",
    "riskLevel": "HIGH",
    "recommendedChecks": "电芯压差检测/BMS均衡功能测试/高温充电测试",
    "confidenceScore": 0.92
  }
}
```

### 风险等级取值

- `LOW`：低风险，可正常行驶
- `MEDIUM`：中风险，建议尽快检修
- `HIGH`：高风险，建议立即停驶检修

## 电池健康预警

记录电池检测数据，自动评估健康等级（NORMAL / WARNING / DANGER），支持 Redis 缓存。

- `POST /api/battery/records` — 新增电池检测记录（自动评估预警等级）
- `GET /api/battery/records/{vin}` — 按 VIN 查所有检测记录
- `GET /api/battery/records/{vin}/latest` — 按 VIN 查最新检测记录
- `GET /api/battery/warnings` — 查询所有 WARNING/DANGER 级别的车辆

### 请求示例

新增电池检测记录：

```json
POST http://localhost:8080/api/battery/records
Content-Type: application/json

{
  "vin": "LGXCE4CB5N0000001",
  "soh": 75.5,
  "chargeCycles": 1200,
  "maxTemperature": 48.0,
  "minTemperature": 2.0,
  "voltageDiff": 0.060
}
```

### 预警等级评估规则

**DANGER**（任一命中即返回）：
- SOH < 60%
- 电芯压差 > 0.100V
- 最高温度 > 55°C
- 最低温度 < -10°C
- 充电循环 > 2000 次

**WARNING**（任一命中且无 DANGER）：
- SOH < 80%
- 电芯压差 ≥ 0.050V
- 最高温度 ≥ 45°C
- 最低温度 ≤ 0°C
- 充电循环 ≥ 1000 次

**NORMAL**：以上均不满足

## 数据库表

模块 4 新增两张表：

```sql
-- Agent 诊断记录
CREATE TABLE agent_diagnosis (
    diagnosis_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    fault_id              BIGINT       NOT NULL,
    input_text            TEXT,
    diagnosis_suggestion  TEXT,
    risk_level            VARCHAR(20),
    recommended_checks    TEXT,
    confidence_score      DECIMAL(5,2),
    agent_name            VARCHAR(50),
    raw_response          TEXT,
    created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 电池健康检测记录
CREATE TABLE battery_health_record (
    battery_record_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    vin                CHAR(17)      NOT NULL,
    soh                DECIMAL(5,2),
    charge_cycles      INT,
    max_temperature    DECIMAL(5,2),
    min_temperature    DECIMAL(5,2),
    voltage_diff       DECIMAL(6,3),
    warning_level      VARCHAR(20)   NOT NULL DEFAULT 'NORMAL',
    detect_time        DATETIME      NOT NULL,
    created_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```
