CREATE DATABASE IF NOT EXISTS byd_aftersale
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE byd_aftersale;

DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS settlement;
DROP TABLE IF EXISTS part_usage;
DROP TABLE IF EXISTS part;
DROP TABLE IF EXISTS work_order;
DROP TABLE IF EXISTS battery_health_record;
DROP TABLE IF EXISTS agent_diagnosis;
DROP TABLE IF EXISTS fault_record;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS vehicle;
DROP TABLE IF EXISTS service_center;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    user_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password     VARCHAR(100) NOT NULL,
    real_name    VARCHAR(50),
    phone        VARCHAR(20)  UNIQUE,
    role         VARCHAR(30)  NOT NULL COMMENT 'OWNER/ADVISOR/TECHNICIAN/PART_ADMIN/SERVICE_MANAGER/ADMIN',
    status       VARCHAR(20)  NOT NULL DEFAULT 'ENABLED',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted      TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE service_center (
    center_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    center_name  VARCHAR(100) NOT NULL,
    city         VARCHAR(50),
    address      VARCHAR(200),
    phone        VARCHAR(20),
    status       VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted      TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE vehicle (
    vin              CHAR(17)     PRIMARY KEY,
    owner_id         BIGINT,
    license_plate    VARCHAR(20),
    model            VARCHAR(50),
    battery_model    VARCHAR(50),
    purchase_date    DATE,
    current_mileage  DECIMAL(10,1),
    vehicle_status   VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted          TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE appointment (
    appointment_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_no    VARCHAR(32) NOT NULL UNIQUE,
    vin               CHAR(17)    NOT NULL,
    owner_id          BIGINT      NOT NULL,
    center_id         BIGINT,
    appointment_time  DATETIME,
    problem_description TEXT,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted           TINYINT     NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE fault_record (
    fault_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    fault_no          VARCHAR(32) NOT NULL UNIQUE,
    appointment_id    BIGINT,
    vin               CHAR(17)    NOT NULL,
    owner_id          BIGINT      NOT NULL,
    advisor_id        BIGINT,
    fault_description TEXT        NOT NULL,
    fault_level       VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status            VARCHAR(30) NOT NULL DEFAULT 'REGISTERED',
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted           TINYINT     NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE agent_diagnosis (
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

CREATE TABLE battery_health_record (
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

CREATE TABLE work_order (
    work_order_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_no   VARCHAR(32)    NOT NULL UNIQUE,
    fault_id        BIGINT         NOT NULL,
    diagnosis_id    BIGINT,
    technician_id   BIGINT,
    status          VARCHAR(30)    NOT NULL DEFAULT 'CREATED',
    labor_cost      DECIMAL(10,2),
    repair_result   TEXT,
    started_at      DATETIME,
    finished_at     DATETIME,
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT        NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE part (
    part_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_no            VARCHAR(32)    NOT NULL UNIQUE,
    part_name          VARCHAR(100)   NOT NULL,
    category           VARCHAR(50),
    stock_quantity     INT            NOT NULL DEFAULT 0,
    warning_threshold  INT,
    purchase_price     DECIMAL(10,2),
    selling_price      DECIMAL(10,2),
    status             VARCHAR(20)    NOT NULL DEFAULT 'ENABLED',
    created_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted            TINYINT        NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE part_usage (
    usage_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id  BIGINT       NOT NULL,
    part_id        BIGINT       NOT NULL,
    quantity       INT          NOT NULL,
    unit_price     DECIMAL(10,2),
    technician_id  BIGINT,
    approved_by    BIGINT,
    status         VARCHAR(20)  NOT NULL DEFAULT 'APPLIED',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at    DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE settlement (
    settlement_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    settlement_no    VARCHAR(32)    NOT NULL UNIQUE,
    work_order_id    BIGINT         NOT NULL UNIQUE,
    labor_amount     DECIMAL(10,2),
    part_amount      DECIMAL(10,2),
    warranty_amount  DECIMAL(10,2),
    total_amount     DECIMAL(10,2),
    payment_status   VARCHAR(20)   NOT NULL DEFAULT 'UNPAID',
    paid_at          DATETIME,
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE operation_log (
    log_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_type  VARCHAR(50),
    business_id    BIGINT,
    action         VARCHAR(100),
    operator_id    BIGINT,
    detail         TEXT,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
