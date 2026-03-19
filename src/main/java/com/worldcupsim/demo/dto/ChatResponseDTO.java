package com.worldcupsim.demo.dto;

import com.worldcupsim.demo.model.BehaviorWeights;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {
    private String playerName;
    private String response;
    private BehaviorWeights updatedWeights;
}
