package com.worldcupsim.demo.engine;

import com.worldcupsim.demo.dto.GameEventDTO;
import com.worldcupsim.demo.dto.MatchStateDTO;
import com.worldcupsim.demo.dto.PlayerStateDTO;
import com.worldcupsim.demo.enums.EventType;
import com.worldcupsim.demo.enums.MatchStatus;
import com.worldcupsim.demo.model.BehaviorWeights;
import com.worldcupsim.demo.model.GameEvent;
import com.worldcupsim.demo.model.Player;
import com.worldcupsim.demo.repository.GameEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimulationEngine {

    @Autowired
    private PhysicsEngine physics;

    @Autowired
    private PlayerDecisionEngine decisionEngine;

    @Autowired
    private GoalDetectionEngine goalDetection;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameEventRepository eventRepository;

    @Value("${game.tick.interval}")
    private int tickInterval;

    private MatchStateDTO currentMatch;
    private int tickCounter = 0;
    private Map<Long, Player> playerMap = new ConcurrentHashMap<>();
    private Map<Long, BehaviorWeightUpdate> pendingBehaviorUpdates = new ConcurrentHashMap<>();

    @Scheduled(fixedRateString = "${game.tick.interval}")
    public void tick() {
        if (currentMatch == null || currentMatch.getStatus() != MatchStatus.PLAYING) {
            return;
        }

        applyBehaviorUpdates();

        for (PlayerStateDTO playerState : currentMatch.getPlayers()) {
            Player player = playerMap.get(playerState.getId());
            if (player == null) continue;

            Point2D.Double target = decisionEngine.decideNextMove(player, currentMatch);
            physics.movePlayer(player, target.getX(), target.getY());

            playerState.setX(player.getX());
            playerState.setY(player.getY());
        }

        physics.updateBall(currentMatch, currentMatch.getBallPossessionPlayerId());

        Long newPossession = physics.checkPossession(currentMatch);
        if (newPossession != null) {
            currentMatch.setBallPossessionPlayerId(newPossession);
        }

        if (goalDetection.isGoal(currentMatch.getBallX(), currentMatch.getBallY())) {
            handleGoal();
        }

        if (tickCounter % 2 == 0) {
            currentMatch.setMinute(currentMatch.getMinute() + 1);

            if (currentMatch.getMinute() == 45) {
                currentMatch.setStatus(MatchStatus.HALFTIME);
            } else if (currentMatch.getMinute() == 90) {
                currentMatch.setStatus(MatchStatus.FINISHED);
            }
        }

        tickCounter++;

        messagingTemplate.convertAndSend("/topic/match", currentMatch);
    }

    private void handleGoal() {
        String side = goalDetection.getGoalSide(currentMatch.getBallX());
        if (side.equals("HOME")) {
            currentMatch.setAwayScore(currentMatch.getAwayScore() + 1);
        } else {
            currentMatch.setHomeScore(currentMatch.getHomeScore() + 1);
        }

        GameEventDTO goalEvent = new GameEventDTO();
        goalEvent.setType(EventType.GOAL);
        goalEvent.setMinute(currentMatch.getMinute());
        goalEvent.setDescription("Goal!");

        if (currentMatch.getRecentEvents() == null) {
            currentMatch.setRecentEvents(new ArrayList<>());
        }
        currentMatch.getRecentEvents().add(0, goalEvent);

        resetToKickoff();
    }

    private void resetToKickoff() {
        currentMatch.setBallX(525.0);
        currentMatch.setBallY(340.0);
    }

    public void queueBehaviorUpdate(Long playerId, BehaviorWeights newWeights) {
        Player player = playerMap.get(playerId);
        if (player != null) {
            pendingBehaviorUpdates.put(playerId,
                new BehaviorWeightUpdate(player.getBehaviorWeights(), newWeights, 10));
        }
    }

    private void applyBehaviorUpdates() {
        pendingBehaviorUpdates.entrySet().removeIf(entry -> {
            Long playerId = entry.getKey();
            BehaviorWeightUpdate update = entry.getValue();
            Player player = playerMap.get(playerId);

            if (player == null) return true;

            double progress = (10 - update.ticksRemaining) / 10.0;
            BehaviorWeights interpolated = interpolate(update.start, update.target, progress);
            player.setBehaviorWeights(interpolated);

            update.ticksRemaining--;
            return update.ticksRemaining <= 0;
        });
    }

    private BehaviorWeights interpolate(BehaviorWeights start, BehaviorWeights target, double progress) {
        return new BehaviorWeights(
            start.getShootTendency() + (target.getShootTendency() - start.getShootTendency()) * progress,
            start.getPassTendency() + (target.getPassTendency() - start.getPassTendency()) * progress,
            start.getAggression() + (target.getAggression() - start.getAggression()) * progress,
            start.getPositioning() + (target.getPositioning() - start.getPositioning()) * progress
        );
    }

    public MatchStateDTO getCurrentState() {
        return currentMatch;
    }

    public void setCurrentMatch(MatchStateDTO match) {
        this.currentMatch = match;
    }

    public void setPlayerMap(Map<Long, Player> playerMap) {
        this.playerMap = playerMap;
    }

    public void pauseMatch() {
        if (currentMatch != null) {
            currentMatch.setStatus(MatchStatus.PAUSED);
        }
    }

    public void resumeMatch() {
        if (currentMatch != null) {
            currentMatch.setStatus(MatchStatus.PLAYING);
        }
    }

    private static class BehaviorWeightUpdate {
        BehaviorWeights start;
        BehaviorWeights target;
        int ticksRemaining;

        BehaviorWeightUpdate(BehaviorWeights start, BehaviorWeights target, int ticksRemaining) {
            this.start = start;
            this.target = target;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
