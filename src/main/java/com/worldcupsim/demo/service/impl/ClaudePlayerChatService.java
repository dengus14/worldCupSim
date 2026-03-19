package com.worldcupsim.demo.service.impl;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldcupsim.demo.dto.ChatResponseDTO;
import com.worldcupsim.demo.dto.MatchStateDTO;
import com.worldcupsim.demo.engine.SimulationEngine;
import com.worldcupsim.demo.model.BehaviorWeights;
import com.worldcupsim.demo.model.Player;
import com.worldcupsim.demo.repository.PlayerRepository;
import com.worldcupsim.demo.service.PlayerChatService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Service
public class ClaudePlayerChatService implements PlayerChatService {

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private SimulationEngine simulationEngine;

    private AnthropicClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }

    @Override
    @Async
    public CompletableFuture<ChatResponseDTO> chat(Long playerId, String message, MatchStateDTO matchState) {
        try {
            Player player = playerRepository.findById(playerId).orElseThrow();

            String prompt = String.format(
                "You are %s, a professional soccer player. Personality: %s. " +
                "Match context: Minute %d, score %d-%d. " +
                "User says: \"%s\". " +
                "Respond in character in 2-3 sentences. " +
                "Then return a JSON block with updated behavior weights: " +
                "{\"shootTendency\": 0.0-1.0, \"passTendency\": 0.0-1.0, \"aggression\": 0.0-1.0, \"positioning\": 0.0-1.0}",
                player.getName(), player.getPersonality(),
                matchState.getMinute(), matchState.getHomeScore(), matchState.getAwayScore(),
                message
            );

            String response = callClaudeAPI(prompt);
            ChatResponseDTO dto = parseResponse(response, player.getName());

            simulationEngine.queueBehaviorUpdate(playerId, dto.getUpdatedWeights());

            return CompletableFuture.completedFuture(dto);
        } catch (Exception e) {
            ChatResponseDTO fallback = new ChatResponseDTO();
            fallback.setPlayerName("Player");
            fallback.setResponse("I'm focused on the match right now.");
            fallback.setUpdatedWeights(new BehaviorWeights(0.5, 0.5, 0.5, 0.5));
            return CompletableFuture.completedFuture(fallback);
        }
    }

    private String callClaudeAPI(String prompt) {
        Message response = client.messages().create(MessageCreateParams.builder()
            .model("claude-3-5-sonnet-20241022")
            .maxTokens(300)
            .addUserMessage(prompt)
            .build());

        return response.content().toString();
    }

    private ChatResponseDTO parseResponse(String rawResponse, String playerName) {
        ChatResponseDTO dto = new ChatResponseDTO();
        dto.setPlayerName(playerName);

        int jsonStart = rawResponse.indexOf('{');
        int jsonEnd = rawResponse.lastIndexOf('}') + 1;

        if (jsonStart != -1 && jsonEnd > jsonStart) {
            String textResponse = rawResponse.substring(0, jsonStart).trim();
            String jsonBlock = rawResponse.substring(jsonStart, jsonEnd);

            dto.setResponse(textResponse);

            try {
                JsonNode json = objectMapper.readTree(jsonBlock);
                BehaviorWeights weights = new BehaviorWeights(
                    json.get("shootTendency").asDouble(),
                    json.get("passTendency").asDouble(),
                    json.get("aggression").asDouble(),
                    json.get("positioning").asDouble()
                );
                dto.setUpdatedWeights(weights);
            } catch (Exception e) {
                dto.setUpdatedWeights(new BehaviorWeights(0.5, 0.5, 0.5, 0.5));
            }
        } else {
            dto.setResponse(rawResponse);
            dto.setUpdatedWeights(new BehaviorWeights(0.5, 0.5, 0.5, 0.5));
        }

        return dto;
    }
}
