package com.byd.aftersales.dto;

public class ChatRequest {

    private Long conversationId;
    private Long faultId;
    private String message;

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getFaultId() { return faultId; }
    public void setFaultId(Long faultId) { this.faultId = faultId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
