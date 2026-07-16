package com.byd.aftersales.dao;

import com.byd.aftersales.domain.AgentConversation;
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
import java.util.Optional;

@Component
public class AgentConversationDao extends BaseJdbcDao {

    private final RowMapper<AgentConversation> rowMapper = (rs, rowNum) -> {
        AgentConversation c = new AgentConversation();
        c.setConversationId(rs.getLong("conversation_id"));
        c.setFaultId(rs.getLong("fault_id"));
        c.setTitle(rs.getString("title"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        c.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        c.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
        return c;
    };

    public AgentConversationDao(DataSource dataSource) {
        super(dataSource);
    }

    public Long insert(AgentConversation conversation) {
        String sql = "INSERT INTO agent_conversation (fault_id, title) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc().update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, conversation.getFaultId());
            ps.setString(2, conversation.getTitle());
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public Optional<AgentConversation> findById(Long conversationId) {
        List<AgentConversation> list = jdbc().query(
                "SELECT * FROM agent_conversation WHERE conversation_id = ?", rowMapper, conversationId);
        return list.stream().findFirst();
    }

    public List<AgentConversation> findByFaultId(Long faultId) {
        return jdbc().query(
                "SELECT * FROM agent_conversation WHERE fault_id = ? ORDER BY created_at DESC",
                rowMapper, faultId);
    }

    public void updateTitle(Long conversationId, String title) {
        jdbc().update("UPDATE agent_conversation SET title = ? WHERE conversation_id = ?", title, conversationId);
    }
}
