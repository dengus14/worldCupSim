package com.worldcupsim.demo.service.impl;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.worldcupsim.demo.dto.MatchStateDTO;
import com.worldcupsim.demo.enums.EventType;
import com.worldcupsim.demo.model.GameEvent;
import com.worldcupsim.demo.service.CommentaryService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ClaudeCommentaryService implements CommentaryService {

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final BlockingQueue<GameEvent> commentaryQueue = new LinkedBlockingQueue<>();
    private AnthropicClient client;
    private MatchStateDTO currentMatchState;

    @PostConstruct
    public void init() {
        client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this::processNextCommentary, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void generateCommentary(GameEvent event, MatchStateDTO matchState) {
        this.currentMatchState = matchState;

        if (event.getType() == EventType.GOAL ||
            event.getType() == EventType.SHOT ||
            event.getType() == EventType.FOUL) {
            commentaryQueue.offer(event);
        }
    }

    @Async
    public void processNextCommentary() {
        GameEvent event = commentaryQueue.poll();
        if (event == null || currentMatchState == null) {
            return;
        }

        try {
            String prompt = buildCommentaryPrompt(event);
            String commentary = callClaudeAPI(prompt);

            Map<String, Object> payload = Map.of("text", commentary, "minute", event.getMinute());
            messagingTemplate.convertAndSend((Object) "/topic/commentary", payload);
        } catch (Exception e) {
            Map<String, Object> fallback = Map.of("text", "What an exciting moment!", "minute", event.getMinute());
            messagingTemplate.convertAndSend((Object) "/topic/commentary", fallback);
        }
    }

    private String callClaudeAPI(String prompt) {
        Message response = client.messages().create(MessageCreateParams.builder()
            .model("claude-3-5-sonnet-20241022")
            .maxTokens(150)
            .addUserMessage(prompt)
            .build());

        return response.content().toString();
    }

    private String buildCommentaryPrompt(GameEvent event) {
        return String.format(
            "You are an enthusiastic professional soccer commentator for the 2026 World Cup. " +
            "Current match: %s vs %s. Score: %d-%d, Minute: %d. " +
            "Event: %s. Generate 1-2 sentences of live commentary. Be energetic.",
            currentMatchState.getHomeTeam(), currentMatchState.getAwayTeam(),
            currentMatchState.getHomeScore(), currentMatchState.getAwayScore(),
            event.getMinute(), event.getDescription()
        );
    }
}
