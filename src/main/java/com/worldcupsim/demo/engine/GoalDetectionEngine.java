package com.worldcupsim.demo.engine;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoalDetectionEngine {

    @Value("${game.pitch.width}")
    private int pitchWidth;

    @Value("${game.goal.y.min}")
    private int goalYMin;

    @Value("${game.goal.y.max}")
    private int goalYMax;

    public boolean isGoal(double ballX, double ballY) {
        boolean inGoalY = ballY >= goalYMin && ballY <= goalYMax;
        return inGoalY && (ballX < 10 || ballX > pitchWidth - 10);
    }

    public String getGoalSide(double ballX) {
        return ballX < 10 ? "HOME" : "AWAY";
    }
}
