package com.worldcupsim.demo.service;

import com.worldcupsim.demo.dto.MatchStateDTO;
import com.worldcupsim.demo.dto.PlayerStateDTO;
import com.worldcupsim.demo.engine.SimulationEngine;
import com.worldcupsim.demo.enums.MatchStatus;
import com.worldcupsim.demo.model.Match;
import com.worldcupsim.demo.model.Player;
import com.worldcupsim.demo.model.Team;
import com.worldcupsim.demo.repository.MatchRepository;
import com.worldcupsim.demo.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MatchService {

    @Autowired
    private TeamInitializationService teamInit;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private SimulationEngine simulationEngine;

    public String startNewMatch() {
        Team france = teamInit.createFranceTeam();
        Team brazil = teamInit.createBrazilTeam();

        teamRepository.save(france);
        teamRepository.save(brazil);

        Match match = new Match();
        match.setId(UUID.randomUUID().toString());
        match.setHomeTeam(france);
        match.setAwayTeam(brazil);
        match.setHomeScore(0);
        match.setAwayScore(0);
        match.setStatus(MatchStatus.PLAYING);
        match.setCreatedAt(LocalDateTime.now());
        matchRepository.save(match);

        MatchStateDTO stateDTO = new MatchStateDTO();
        stateDTO.setMatchId(match.getId());
        stateDTO.setHomeTeam(france.getName());
        stateDTO.setAwayTeam(brazil.getName());
        stateDTO.setHomeScore(0);
        stateDTO.setAwayScore(0);
        stateDTO.setMinute(0);
        stateDTO.setStatus(MatchStatus.PLAYING);
        stateDTO.setBallX(525.0);
        stateDTO.setBallY(340.0);
        stateDTO.setBallPossessionPlayerId(null);
        stateDTO.setRecentEvents(new ArrayList<>());

        Map<Long, Player> playerMap = new HashMap<>();
        for (Player p : france.getPlayers()) {
            playerMap.put(p.getId(), p);
        }
        for (Player p : brazil.getPlayers()) {
            playerMap.put(p.getId(), p);
        }

        stateDTO.setPlayers(
            playerMap.values().stream()
                .map(this::toPlayerStateDTO)
                .collect(Collectors.toList())
        );

        simulationEngine.setCurrentMatch(stateDTO);
        simulationEngine.setPlayerMap(playerMap);

        return match.getId();
    }

    public MatchStateDTO getCurrentMatchState(String matchId) {
        return simulationEngine.getCurrentState();
    }

    public void pauseMatch(String matchId) {
        simulationEngine.pauseMatch();
    }

    public void resumeMatch(String matchId) {
        simulationEngine.resumeMatch();
    }

    private PlayerStateDTO toPlayerStateDTO(Player player) {
        PlayerStateDTO dto = new PlayerStateDTO();
        dto.setId(player.getId());
        dto.setName(player.getName());
        dto.setTeamId(player.getTeam().getId().toString());
        dto.setPosition(player.getPosition());
        dto.setX(player.getX());
        dto.setY(player.getY());
        dto.setHasBall(false);
        return dto;
    }
}
