package com.byd.aftersales.dto;

import com.byd.aftersales.domain.AgentMessage;

import java.util.List;

public class ChatResponse {

    private Long conversationId;
    private String reply;
    private List<AgentMessage> history;

    public ChatResponse() {}

    public ChatResponse(Long conversationId, String reply, List<AgentMessage> history) {
        this.conversationId = conversationId;
        this.reply = reply;
        this.history = history;
    }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public List<AgentMessage> getHistory() { return history; }
    public void setHistory(List<AgentMessage> history) { this.history = history; }
}
