-- 车辆表增加负责顾问外键（已有库升级用，重复执行前请确认列是否已存在）
USE byd_aftersales;

ALTER TABLE vehicle ADD COLUMN advisor_id BIGINT NULL AFTER owner_id;

UPDATE vehicle v
SET advisor_id = (SELECT user_id FROM sys_user WHERE username = 'advisor001' LIMIT 1)
WHERE v.advisor_id IS NULL;

UPDATE vehicle SET advisor_id = (SELECT user_id FROM sys_user WHERE username = 'advisor001' LIMIT 1)
WHERE vin IN ('LC0CE4DB7N0000001', 'LC0CE4DB7N0000002', 'LC0CE4DB7N0000003', 'LC0CE4DB7N0000004',
              'LC0CE4DB7N0000007', 'LC0CE4DB7N0000009');

UPDATE vehicle SET advisor_id = (SELECT user_id FROM sys_user WHERE username = 'advisor002' LIMIT 1)
WHERE vin IN ('LC0CE4DB7N0000005', 'LC0CE4DB7N0000006', 'LC0CE4DB7N0000008', 'LC0CE4DB7N0000010');

ALTER TABLE vehicle MODIFY advisor_id BIGINT NOT NULL;
ALTER TABLE vehicle ADD KEY idx_vehicle_advisor_id (advisor_id);
ALTER TABLE vehicle ADD CONSTRAINT fk_vehicle_advisor FOREIGN KEY (advisor_id) REFERENCES sys_user (user_id);
