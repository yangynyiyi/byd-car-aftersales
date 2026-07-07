-- 比亚迪售后系统 - 第三部分数据表 DDL
-- 依赖第二部分已建好: sys_user, vehicle, fault_record

CREATE DATABASE IF NOT EXISTS byd_car DEFAULT CHARACTER SET utf8mb4;
USE byd_car;

-- 维修工单
CREATE TABLE IF NOT EXISTS work_order (
    work_order_id   BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    work_order_no   VARCHAR(32)     NOT NULL COMMENT '工单编号（唯一）',
    fault_id        BIGINT          NOT NULL COMMENT '关联故障记录',
    diagnosis_id    BIGINT          DEFAULT NULL COMMENT '关联 Agent 诊断（可为空）',
    technician_id   BIGINT          DEFAULT NULL COMMENT '维修技师 ID',
    status          VARCHAR(30)     NOT NULL DEFAULT 'CREATED'
                        COMMENT 'CREATED/ASSIGNED/IN_PROGRESS/PART_WAITING/COMPLETED/CANCELLED',
    labor_cost      DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '工时费',
    repair_result   TEXT            DEFAULT NULL COMMENT '维修结果',
    started_at      DATETIME        DEFAULT NULL COMMENT '开始维修时间',
    finished_at     DATETIME        DEFAULT NULL COMMENT '完工时间',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '0未删除 1已删除',
    PRIMARY KEY (work_order_id),
    UNIQUE KEY uk_work_order_no (work_order_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '维修工单';

-- 备件
CREATE TABLE IF NOT EXISTS part (
    part_id           BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    part_no           VARCHAR(32)     NOT NULL COMMENT '备件编号（唯一）',
    part_name         VARCHAR(100)    NOT NULL COMMENT '备件名称',
    category          VARCHAR(50)     DEFAULT NULL COMMENT '备件分类',
    stock_quantity    INT             NOT NULL DEFAULT 0 COMMENT '当前库存',
    warning_threshold INT             NOT NULL DEFAULT 10 COMMENT '低库存预警阈值',
    purchase_price    DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '采购价',
    selling_price     DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '售价',
    status            VARCHAR(20)     NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted           TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (part_id),
    UNIQUE KEY uk_part_no (part_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '备件';

-- 备件使用记录
CREATE TABLE IF NOT EXISTS part_usage (
    usage_id       BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    work_order_id  BIGINT          NOT NULL COMMENT '关联工单',
    part_id        BIGINT          NOT NULL COMMENT '关联备件',
    quantity       INT             NOT NULL COMMENT '使用数量',
    unit_price     DECIMAL(10, 2)  NOT NULL COMMENT '使用时售价（快照）',
    technician_id  BIGINT          NOT NULL COMMENT '申请技师',
    approved_by    BIGINT          DEFAULT NULL COMMENT '审批人',
    status         VARCHAR(20)     NOT NULL DEFAULT 'APPLIED'
                       COMMENT 'APPLIED/APPROVED/REJECTED/USED/RETURNED',
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at    DATETIME        DEFAULT NULL COMMENT '审批时间',
    PRIMARY KEY (usage_id),
    KEY idx_work_order_id (work_order_id),
    KEY idx_part_id (part_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '备件使用记录';

-- 维修结算单
CREATE TABLE IF NOT EXISTS settlement (
    settlement_id    BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    settlement_no    VARCHAR(32)     NOT NULL COMMENT '结算单编号（唯一）',
    work_order_id    BIGINT          NOT NULL COMMENT '关联工单（唯一）',
    labor_amount     DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '工时费',
    part_amount      DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '备件费',
    warranty_amount  DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '质保抵扣',
    total_amount     DECIMAL(10, 2)  NOT NULL DEFAULT 0.00 COMMENT '实收总金额',
    payment_status   VARCHAR(20)     NOT NULL DEFAULT 'UNPAID' COMMENT 'UNPAID/PAID/REFUNDED',
    paid_at          DATETIME        DEFAULT NULL COMMENT '支付时间',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (settlement_id),
    UNIQUE KEY uk_settlement_no (settlement_no),
    UNIQUE KEY uk_work_order_id (work_order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '维修结算单';
