package com.worldcupsim.demo.service;

import com.worldcupsim.demo.dto.MatchStateDTO;
import com.worldcupsim.demo.model.GameEvent;

public interface CommentaryService {
    void generateCommentary(GameEvent event, MatchStateDTO matchState);
}
