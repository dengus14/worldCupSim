package com.worldcupsim.demo.dto;

import com.worldcupsim.demo.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameEventDTO {
    private EventType type;
    private Long playerId;
    private Long teamId;
    private Integer minute;
    private Double x;
    private Double y;
    private String description;
}
