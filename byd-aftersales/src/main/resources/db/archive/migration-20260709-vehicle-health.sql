-- 车辆健康后端模型迁移：已有库执行一次即可；表已存在可忽略
USE byd_aftersales;

CREATE TABLE IF NOT EXISTS vehicle_health_snapshot (
    snapshot_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vin CHAR(17) NOT NULL,
    health_score INT NOT NULL DEFAULT 100,
    overall_level VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    summary VARCHAR(200) NOT NULL,
    suggestion TEXT,
    detect_time DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_vehicle_health_vin_time (vin, detect_time),
    CONSTRAINT fk_vehicle_health_snapshot_vehicle FOREIGN KEY (vin) REFERENCES vehicle (vin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vehicle_health_item (
    item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    snapshot_id BIGINT NOT NULL,
    item_type VARCHAR(40) NOT NULL,
    item_name VARCHAR(80) NOT NULL,
    level VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    metric_value VARCHAR(80),
    description TEXT,
    action_suggestion VARCHAR(200),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_vehicle_health_item_snapshot (snapshot_id),
    KEY idx_vehicle_health_item_type_level (item_type, level),
    CONSTRAINT fk_vehicle_health_item_snapshot FOREIGN KEY (snapshot_id) REFERENCES vehicle_health_snapshot (snapshot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
