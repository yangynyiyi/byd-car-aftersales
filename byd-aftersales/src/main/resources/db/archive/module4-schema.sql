-- Module 4: Agent 诊断 + 电池健康预警 建表

CREATE TABLE IF NOT EXISTS agent_diagnosis (
    diagnosis_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    fault_id              BIGINT       NOT NULL,
    input_text            TEXT,
    diagnosis_suggestion  TEXT,
    risk_level            VARCHAR(20)  COMMENT 'LOW/MEDIUM/HIGH',
    recommended_checks    TEXT,
    confidence_score      DECIMAL(5,2),
    agent_name            VARCHAR(50),
    raw_response          TEXT,
    created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS battery_health_record (
    battery_record_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    vin                CHAR(17)      NOT NULL,
    soh                DECIMAL(5,2)  COMMENT 'SOH percent',
    charge_cycles      INT,
    max_temperature    DECIMAL(5,2),
    min_temperature    DECIMAL(5,2),
    voltage_diff       DECIMAL(6,3),
    warning_level      VARCHAR(20)   NOT NULL DEFAULT 'NORMAL',
    detect_time        DATETIME      NOT NULL,
    created_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
