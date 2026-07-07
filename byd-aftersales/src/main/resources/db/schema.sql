CREATE DATABASE IF NOT EXISTS byd_aftersales DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE byd_aftersales;

-- 用户表：统一存放车主、售后顾问、维修技师、备件管理员、服务经理和系统管理员。
CREATE TABLE IF NOT EXISTS sys_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_phone (phone),
    KEY idx_sys_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 服务中心表：存放比亚迪售后服务门店信息。
CREATE TABLE IF NOT EXISTS service_center (
    center_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    center_name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    address VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 车辆档案表：B 同学核心负责表，VIN 为车辆唯一识别码。
CREATE TABLE IF NOT EXISTS vehicle (
    vin CHAR(17) PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    license_plate VARCHAR(20),
    model VARCHAR(50) NOT NULL,
    battery_model VARCHAR(50) NOT NULL,
    purchase_date DATE,
    current_mileage DECIMAL(10,1) NOT NULL DEFAULT 0,
    vehicle_status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_vehicle_owner_id (owner_id),
    CONSTRAINT fk_vehicle_owner FOREIGN KEY (owner_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 预约表：记录车主提交的保养或维修预约。
CREATE TABLE IF NOT EXISTS appointment (
    appointment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_no VARCHAR(32) NOT NULL,
    vin CHAR(17) NOT NULL,
    owner_id BIGINT NOT NULL,
    center_id BIGINT NOT NULL,
    appointment_time DATETIME NOT NULL,
    problem_description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_appointment_no (appointment_no),
    KEY idx_appointment_vin (vin),
    KEY idx_appointment_owner_id (owner_id),
    KEY idx_appointment_center_id (center_id),
    CONSTRAINT fk_appointment_vehicle FOREIGN KEY (vin) REFERENCES vehicle (vin),
    CONSTRAINT fk_appointment_owner FOREIGN KEY (owner_id) REFERENCES sys_user (user_id),
    CONSTRAINT fk_appointment_center FOREIGN KEY (center_id) REFERENCES service_center (center_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 故障登记表：售后顾问接车后登记故障现象，后续 Agent 诊断和维修工单都依赖此表。
CREATE TABLE IF NOT EXISTS fault_record (
    fault_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fault_no VARCHAR(32) NOT NULL,
    appointment_id BIGINT,
    vin CHAR(17) NOT NULL,
    owner_id BIGINT NOT NULL,
    advisor_id BIGINT NOT NULL,
    fault_description TEXT NOT NULL,
    fault_level VARCHAR(20) NOT NULL DEFAULT 'LOW',
    status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_fault_no (fault_no),
    KEY idx_fault_vin (vin),
    KEY idx_fault_appointment_id (appointment_id),
    CONSTRAINT fk_fault_appointment FOREIGN KEY (appointment_id) REFERENCES appointment (appointment_id),
    CONSTRAINT fk_fault_vehicle FOREIGN KEY (vin) REFERENCES vehicle (vin),
    CONSTRAINT fk_fault_owner FOREIGN KEY (owner_id) REFERENCES sys_user (user_id),
    CONSTRAINT fk_fault_advisor FOREIGN KEY (advisor_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Agent 诊断记录表：记录远程 Agent 返回的诊断建议、风险等级和推荐检测项目。
CREATE TABLE IF NOT EXISTS agent_diagnosis (
    diagnosis_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fault_id BIGINT NOT NULL,
    input_text TEXT NOT NULL,
    diagnosis_suggestion TEXT NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    recommended_checks TEXT,
    confidence_score DECIMAL(5,2),
    agent_name VARCHAR(50),
    raw_response TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_agent_diagnosis_fault_id (fault_id),
    CONSTRAINT fk_agent_diagnosis_fault FOREIGN KEY (fault_id) REFERENCES fault_record (fault_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 电池健康记录表：一辆车可以有多次电池检测记录。
CREATE TABLE IF NOT EXISTS battery_health_record (
    battery_record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vin CHAR(17) NOT NULL,
    soh DECIMAL(5,2) NOT NULL,
    charge_cycles INT NOT NULL DEFAULT 0,
    max_temperature DECIMAL(5,2),
    min_temperature DECIMAL(5,2),
    voltage_diff DECIMAL(6,3),
    warning_level VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    detect_time DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_battery_vin_detect_time (vin, detect_time),
    CONSTRAINT fk_battery_vehicle FOREIGN KEY (vin) REFERENCES vehicle (vin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 维修工单表：根据故障登记生成维修工单，分派给维修技师处理。
CREATE TABLE IF NOT EXISTS work_order (
    work_order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_order_no VARCHAR(32) NOT NULL,
    fault_id BIGINT NOT NULL,
    diagnosis_id BIGINT,
    technician_id BIGINT,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    labor_cost DECIMAL(10,2) NOT NULL DEFAULT 0,
    repair_result TEXT,
    started_at DATETIME,
    finished_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_work_order_no (work_order_no),
    KEY idx_work_order_fault_id (fault_id),
    CONSTRAINT fk_work_order_fault FOREIGN KEY (fault_id) REFERENCES fault_record (fault_id),
    CONSTRAINT fk_work_order_diagnosis FOREIGN KEY (diagnosis_id) REFERENCES agent_diagnosis (diagnosis_id),
    CONSTRAINT fk_work_order_technician FOREIGN KEY (technician_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 备件表：记录维修备件库存和价格信息。
CREATE TABLE IF NOT EXISTS part (
    part_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    part_no VARCHAR(32) NOT NULL,
    part_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    warning_threshold INT NOT NULL DEFAULT 0,
    purchase_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    selling_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_part_no (part_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 备件领用表：记录维修工单中申请和使用的备件。
CREATE TABLE IF NOT EXISTS part_usage (
    usage_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_order_id BIGINT NOT NULL,
    part_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    technician_id BIGINT NOT NULL,
    approved_by BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'APPLIED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at DATETIME,
    KEY idx_part_usage_work_order_id (work_order_id),
    CONSTRAINT fk_part_usage_work_order FOREIGN KEY (work_order_id) REFERENCES work_order (work_order_id),
    CONSTRAINT fk_part_usage_part FOREIGN KEY (part_id) REFERENCES part (part_id),
    CONSTRAINT fk_part_usage_technician FOREIGN KEY (technician_id) REFERENCES sys_user (user_id),
    CONSTRAINT fk_part_usage_approved_by FOREIGN KEY (approved_by) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 维修结算表：根据维修工单、工时费和备件费生成结算单。
CREATE TABLE IF NOT EXISTS settlement (
    settlement_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    settlement_no VARCHAR(32) NOT NULL,
    work_order_id BIGINT NOT NULL,
    labor_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    part_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    warranty_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    paid_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_settlement_no (settlement_no),
    UNIQUE KEY uk_settlement_work_order_id (work_order_id),
    CONSTRAINT fk_settlement_work_order FOREIGN KEY (work_order_id) REFERENCES work_order (work_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 操作日志表：记录关键业务操作，可用于事务控制和测试展示。
CREATE TABLE IF NOT EXISTS operation_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_type VARCHAR(50) NOT NULL,
    business_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    operator_id BIGINT,
    detail TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_operation_log_business (business_type, business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
