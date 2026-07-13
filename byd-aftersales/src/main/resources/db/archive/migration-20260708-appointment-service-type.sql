-- 预约服务类型迁移：已有库执行一次即可；列或索引已存在可忽略报错
USE byd_aftersales;

ALTER TABLE appointment
    ADD COLUMN service_type VARCHAR(30) NOT NULL DEFAULT 'FAULT_REPAIR' AFTER appointment_time;

CREATE INDEX idx_appointment_service_type ON appointment (service_type);

UPDATE appointment
SET service_type = CASE
    WHEN problem_description LIKE '%年检%' THEN 'ANNUAL_INSPECTION'
    WHEN problem_description LIKE '%保养%' OR problem_description LIKE '%健诊%' OR problem_description LIKE '%检测%' THEN 'SCHEDULED_MAINTENANCE'
    WHEN problem_description LIKE '%救援%' OR problem_description LIKE '%拖车%' OR problem_description LIKE '%无法行驶%' OR problem_description LIKE '%突发%' THEN 'EMERGENCY_RESCUE'
    ELSE 'FAULT_REPAIR'
END
WHERE service_type IS NULL OR service_type = '' OR service_type = 'FAULT_REPAIR';
