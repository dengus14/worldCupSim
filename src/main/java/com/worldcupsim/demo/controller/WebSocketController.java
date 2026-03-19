package com.worldcupsim.demo.controller;

import com.worldcupsim.demo.dto.ChatMessageDTO;
import com.worldcupsim.demo.dto.ChatResponseDTO;
import com.worldcupsim.demo.service.MatchService;
import com.worldcupsim.demo.service.PlayerChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private MatchService matchService;

    @Autowired
    private PlayerChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/pause")
    public void pauseMatch() {
        matchService.pauseMatch(null);
    }

    @MessageMapping("/resume")
    public void resumeMatch() {
        matchService.resumeMatch(null);
    }

    @MessageMapping("/chat")
    public void playerChat(ChatMessageDTO chatMessage) {
        chatService.chat(
            chatMessage.getPlayerId(),
            chatMessage.getMessage(),
            matchService.getCurrentMatchState(null)
        ).thenAccept(response -> {
            messagingTemplate.convertAndSend(
                "/topic/chat/" + chatMessage.getPlayerId(),
                response
            );
        });
    }
}
