package com.worldcupsim.demo.controller;

import com.worldcupsim.demo.dto.MatchStateDTO;
import com.worldcupsim.demo.model.Player;
import com.worldcupsim.demo.repository.PlayerRepository;
import com.worldcupsim.demo.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/match")
@CrossOrigin(origins = "http://localhost:3000")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @Autowired
    private PlayerRepository playerRepository;

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startMatch() {
        String matchId = matchService.startNewMatch();
        return ResponseEntity.ok(Map.of("matchId", matchId));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchStateDTO> getMatchState(@PathVariable String matchId) {
        if (matchId == null || matchId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        MatchStateDTO state = matchService.getCurrentMatchState(matchId);
        if (state == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(state);
    }

    @GetMapping("/{matchId}/players")
    public ResponseEntity<List<Player>> getPlayers(@PathVariable String matchId) {
        if (matchId == null || matchId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<Player> players = playerRepository.findAll();
        return ResponseEntity.ok(players);
    }
}
