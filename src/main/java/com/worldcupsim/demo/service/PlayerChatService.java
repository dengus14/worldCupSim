package com.worldcupsim.demo.service;

import com.worldcupsim.demo.dto.ChatResponseDTO;
import com.worldcupsim.demo.dto.MatchStateDTO;

import java.util.concurrent.CompletableFuture;

public interface PlayerChatService {
    CompletableFuture<ChatResponseDTO> chat(Long playerId, String message, MatchStateDTO matchState);
}
