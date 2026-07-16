-- Agent 多轮对话支持
-- 执行: mysql -uroot -p1234 byd_aftersales < migration-20260715-agent-chat.sql

USE byd_aftersales;

-- 对话会话表
CREATE TABLE IF NOT EXISTS agent_conversation (
    conversation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fault_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL DEFAULT '诊断对话',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_conversation_fault_id (fault_id),
    CONSTRAINT fk_conversation_fault FOREIGN KEY (fault_id) REFERENCES fault_record (fault_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 对话消息表
CREATE TABLE IF NOT EXISTS agent_message (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'user 或 assistant',
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_message_conversation_id (conversation_id),
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES agent_conversation (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
