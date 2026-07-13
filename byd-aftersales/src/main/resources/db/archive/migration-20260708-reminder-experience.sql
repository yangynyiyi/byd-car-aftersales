-- 售后体验深化迁移：已有库执行一次即可；列、表或索引已存在可忽略报错
USE byd_aftersales;

ALTER TABLE vehicle ADD COLUMN last_maintenance_date DATE NULL AFTER purchase_date;
ALTER TABLE vehicle ADD COLUMN next_maintenance_date DATE NULL AFTER last_maintenance_date;
ALTER TABLE vehicle ADD COLUMN next_inspection_date DATE NULL AFTER next_maintenance_date;
ALTER TABLE vehicle ADD COLUMN insurance_expire_date DATE NULL AFTER next_inspection_date;

CREATE TABLE IF NOT EXISTS vehicle_reminder (
    reminder_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reminder_no VARCHAR(32) NOT NULL,
    vin CHAR(17) NOT NULL,
    owner_id BIGINT NOT NULL,
    reminder_type VARCHAR(30) NOT NULL,
    level VARCHAR(20) NOT NULL DEFAULT 'INFO',
    title VARCHAR(100) NOT NULL,
    content TEXT,
    due_time DATETIME,
    status VARCHAR(20) NOT NULL DEFAULT 'UNREAD',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_vehicle_reminder_no (reminder_no),
    KEY idx_vehicle_reminder_owner_status (owner_id, status),
    KEY idx_vehicle_reminder_vin (vin),
    CONSTRAINT fk_vehicle_reminder_vehicle FOREIGN KEY (vin) REFERENCES vehicle (vin),
    CONSTRAINT fk_vehicle_reminder_owner FOREIGN KEY (owner_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

UPDATE vehicle
SET last_maintenance_date = COALESCE(last_maintenance_date, DATE_SUB(CURDATE(), INTERVAL 5 MONTH)),
    next_maintenance_date = COALESCE(next_maintenance_date, DATE_ADD(CURDATE(), INTERVAL 10 DAY)),
    next_inspection_date = COALESCE(next_inspection_date, DATE_ADD(CURDATE(), INTERVAL 25 DAY)),
    insurance_expire_date = COALESCE(insurance_expire_date, DATE_ADD(CURDATE(), INTERVAL 40 DAY))
WHERE deleted = 0;
