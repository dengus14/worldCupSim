package com.worldcupsim.demo.dto;

import com.worldcupsim.demo.enums.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchStateDTO {
    private String matchId;
    private String homeTeam;
    private String awayTeam;
    private Integer homeScore;
    private Integer awayScore;
    private Integer minute;
    private MatchStatus status;
    private Double ballX;
    private Double ballY;
    private Long ballPossessionPlayerId;
    private List<PlayerStateDTO> players;
    private List<GameEventDTO> recentEvents;
}
