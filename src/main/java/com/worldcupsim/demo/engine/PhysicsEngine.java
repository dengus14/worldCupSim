package com.worldcupsim.demo.engine;

import com.worldcupsim.demo.dto.MatchStateDTO;
import com.worldcupsim.demo.dto.PlayerStateDTO;
import com.worldcupsim.demo.model.Player;
import org.springframework.stereotype.Component;

@Component
public class PhysicsEngine {

    public void movePlayer(Player player, double targetX, double targetY) {
        double dx = targetX - player.getX();
        double dy = targetY - player.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 5) {
            return;
        }

        double moveDistance = player.getSpeed() * 20;
        double ratio = Math.min(1.0, moveDistance / distance);

        player.setX(player.getX() + dx * ratio);
        player.setY(player.getY() + dy * ratio);
    }

    public void updateBall(MatchStateDTO state, Long possessionPlayerId) {
        if (possessionPlayerId == null) {
            return;
        }

        PlayerStateDTO possessor = state.getPlayers().stream()
                .filter(p -> p.getId().equals(possessionPlayerId))
                .findFirst()
                .orElse(null);

        if (possessor != null) {
            state.setBallX(possessor.getX());
            state.setBallY(possessor.getY());
        }
    }

    public Long checkPossession(MatchStateDTO state) {
        for (PlayerStateDTO p : state.getPlayers()) {
            double dist = distance(p.getX(), p.getY(), state.getBallX(), state.getBallY());
            if (dist < 10) {
                return p.getId();
            }
        }
        return null;
    }

    private double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
