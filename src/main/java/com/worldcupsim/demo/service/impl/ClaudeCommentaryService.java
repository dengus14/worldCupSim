package com.worldcupsim.demo.service.impl;

import com.anthropic.Anthropic;
import com.anthropic.models.Message;
import com.anthropic.models.MessageCreateParams;
import com.anthropic.models.MessageParam;
import com.anthropic.models.TextBlock;
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
    private Anthropic client;
    private MatchStateDTO currentMatchState;

    @PostConstruct
    public void init() {
        client = Anthropic.builder().apiKey(apiKey).build();

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

            messagingTemplate.convertAndSend("/topic/commentary",
                Map.of("text", commentary, "minute", event.getMinute()));
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/commentary",
                Map.of("text", "What an exciting moment!", "minute", event.getMinute()));
        }
    }

    private String callClaudeAPI(String prompt) {
        Message response = client.messages().create(MessageCreateParams.builder()
            .model("claude-3-5-sonnet-20241022")
            .maxTokens(150)
            .messages(Collections.singletonList(
                MessageParam.builder()
                    .role(MessageParam.Role.USER)
                    .content(prompt)
                    .build()
            ))
            .build());

        return ((TextBlock) response.content().get(0)).text();
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
