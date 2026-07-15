-- 问题修复迁移：工单时间戳字段 + 技师工时费配置
-- 已有库执行：mysql -uroot -p123456 byd_aftersales < migration-20260714-issue-fixes.sql

USE byd_aftersales;

ALTER TABLE work_order
    ADD COLUMN assigned_at DATETIME NULL AFTER technician_id,
    ADD COLUMN part_waiting_at DATETIME NULL AFTER started_at,
    ADD COLUMN parts_arrived_at DATETIME NULL AFTER part_waiting_at;

CREATE TABLE IF NOT EXISTS technician_rate (
    rate_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    technician_id BIGINT NOT NULL,
    hourly_rate DECIMAL(10,2) NOT NULL DEFAULT 0,
    daily_rate DECIMAL(10,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_technician_rate (technician_id),
    CONSTRAINT fk_technician_rate_user FOREIGN KEY (technician_id) REFERENCES sys_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO technician_rate (technician_id, hourly_rate, daily_rate, status)
SELECT u.user_id, 280.00, 350.00, 'ENABLED'
FROM sys_user u WHERE u.username = 'tech001'
  AND NOT EXISTS (SELECT 1 FROM technician_rate tr WHERE tr.technician_id = u.user_id);

INSERT INTO technician_rate (technician_id, hourly_rate, daily_rate, status)
SELECT u.user_id, 320.00, 400.00, 'ENABLED'
FROM sys_user u WHERE u.username = 'tech002'
  AND NOT EXISTS (SELECT 1 FROM technician_rate tr WHERE tr.technician_id = u.user_id);

UPDATE work_order
SET assigned_at = COALESCE(assigned_at, updated_at, created_at)
WHERE technician_id IS NOT NULL AND assigned_at IS NULL AND deleted = 0;

UPDATE work_order
SET part_waiting_at = updated_at
WHERE status = 'PART_WAITING' AND part_waiting_at IS NULL AND deleted = 0;
