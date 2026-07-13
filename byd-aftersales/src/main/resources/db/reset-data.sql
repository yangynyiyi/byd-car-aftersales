-- =============================================================================
-- 清空业务数据（保留表结构）
-- 用法（Windows 务必带 utf8mb4，否则中文会变成 ????）：
--   mysql -uroot -p123456 --default-character-set=utf8mb4 byd_aftersales < reset-data.sql
--   mysql -uroot -p123456 --default-character-set=utf8mb4 byd_aftersales < seed-production.sql
-- =============================================================================
USE byd_aftersales;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE operation_log;
TRUNCATE TABLE vehicle_health_item;
TRUNCATE TABLE vehicle_reminder;
TRUNCATE TABLE part_usage;
TRUNCATE TABLE settlement;
TRUNCATE TABLE work_order;
TRUNCATE TABLE agent_diagnosis;
TRUNCATE TABLE fault_record;
TRUNCATE TABLE appointment;
TRUNCATE TABLE battery_health_record;
TRUNCATE TABLE vehicle_health_snapshot;
TRUNCATE TABLE vehicle;
TRUNCATE TABLE part;
TRUNCATE TABLE service_center;
TRUNCATE TABLE sys_user;

SET FOREIGN_KEY_CHECKS = 1;
