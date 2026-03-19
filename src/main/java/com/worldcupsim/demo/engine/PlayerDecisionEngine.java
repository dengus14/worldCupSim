package com.worldcupsim.demo.engine;

import com.worldcupsim.demo.dto.MatchStateDTO;
import com.worldcupsim.demo.dto.PlayerStateDTO;
import com.worldcupsim.demo.enums.Position;
import com.worldcupsim.demo.model.BehaviorWeights;
import com.worldcupsim.demo.model.Player;
import org.springframework.stereotype.Component;

import java.awt.geom.Point2D;
import java.util.Random;

@Component
public class PlayerDecisionEngine {

    private final Random random = new Random();

    public Point2D.Double decideNextMove(Player player, MatchStateDTO state) {
        if (player.getId().equals(state.getBallPossessionPlayerId())) {
            return decideWithBall(player, state);
        } else {
            return decideWithoutBall(player, state);
        }
    }

    private Point2D.Double decideWithBall(Player player, MatchStateDTO state) {
        BehaviorWeights weights = player.getBehaviorWeights();
        Position position = player.getPosition();

        switch (position) {
            case FWD:
                if (weights.getShootTendency() > 0.6 && nearGoal(player)) {
                    return aimAtGoal(player, state);
                }
                return findPassTarget(player, state);

            case MID:
                return findPassTarget(player, state);

            case DEF:
                return clearBall(player);

            case GK:
                return new Point2D.Double(player.getX() + 200, 340);

            default:
                return new Point2D.Double(player.getX(), player.getY());
        }
    }

    private Point2D.Double decideWithoutBall(Player player, MatchStateDTO state) {
        Position position = player.getPosition();

        switch (position) {
            case FWD:
                return new Point2D.Double(900 + randomOffset(), 340 + randomOffset());

            case MID:
                return supportPosition(player, state);

            case DEF:
                return trackOpponent(player, state);

            case GK:
                return new Point2D.Double(50, 340);

            default:
                return new Point2D.Double(player.getX(), player.getY());
        }
    }

    private boolean nearGoal(Player player) {
        return player.getX() > 800;
    }

    private Point2D.Double aimAtGoal(Player player, MatchStateDTO state) {
        return new Point2D.Double(1050, 340 + randomOffset());
    }

    private Point2D.Double findPassTarget(Player player, MatchStateDTO state) {
        return new Point2D.Double(player.getX() + 100, player.getY() + randomOffset());
    }

    private Point2D.Double clearBall(Player player) {
        return new Point2D.Double(player.getX() + 200, 340);
    }

    private Point2D.Double supportPosition(Player player, MatchStateDTO state) {
        return new Point2D.Double(525, 340 + randomOffset());
    }

    private Point2D.Double trackOpponent(Player player, MatchStateDTO state) {
        return new Point2D.Double(player.getX() - 50, player.getY());
    }

    private double randomOffset() {
        return (random.nextDouble() - 0.5) * 40;
    }
}
