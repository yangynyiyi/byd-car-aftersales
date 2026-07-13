-- P0 功能迁移：结算经理审核字段（已有库执行一次即可；列已存在可忽略报错）
USE byd_aftersales;

ALTER TABLE settlement
    ADD COLUMN manager_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL' AFTER payment_status;

ALTER TABLE settlement
    ADD COLUMN approved_by BIGINT NULL AFTER manager_status;

ALTER TABLE settlement
    ADD COLUMN approved_at DATETIME NULL AFTER approved_by;

UPDATE settlement SET manager_status = 'APPROVED' WHERE manager_status = 'PENDING_APPROVAL';
