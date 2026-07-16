package com.byd.aftersales.dao;

import com.byd.aftersales.domain.AgentMessage;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Component
public class AgentMessageDao extends BaseJdbcDao {

    private final RowMapper<AgentMessage> rowMapper = (rs, rowNum) -> {
        AgentMessage m = new AgentMessage();
        m.setMessageId(rs.getLong("message_id"));
        m.setConversationId(rs.getLong("conversation_id"));
        m.setRole(rs.getString("role"));
        m.setContent(rs.getString("content"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        m.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return m;
    };

    public AgentMessageDao(DataSource dataSource) {
        super(dataSource);
    }

    public Long insert(AgentMessage message) {
        String sql = "INSERT INTO agent_message (conversation_id, role, content) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc().update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, message.getConversationId());
            ps.setString(2, message.getRole());
            ps.setString(3, message.getContent());
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public List<AgentMessage> findByConversationId(Long conversationId) {
        return jdbc().query(
                "SELECT * FROM agent_message WHERE conversation_id = ? ORDER BY created_at ASC",
                rowMapper, conversationId);
    }
}
