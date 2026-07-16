package com.byd.aftersales.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.aftersales.domain.AgentConversation;
import com.byd.aftersales.domain.AgentMessage;
import com.byd.aftersales.dto.ChatRequest;
import com.byd.aftersales.dto.ChatResponse;
import com.byd.aftersales.service.ConversationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
public class AgentChatController {

    private final ConversationService conversationService;

    public AgentChatController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping("/chat")
    public ApiResponse<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ApiResponse.ok(conversationService.chat(request));
    }

    @GetMapping("/chat/conversations/{faultId}")
    public ApiResponse<List<AgentConversation>> listConversations(@PathVariable("faultId") Long faultId) {
        return ApiResponse.ok(conversationService.listByFaultId(faultId));
    }

    @GetMapping("/chat/history/{conversationId}")
    public ApiResponse<List<AgentMessage>> getHistory(@PathVariable("conversationId") Long conversationId) {
        return ApiResponse.ok(conversationService.getHistory(conversationId));
    }
}
