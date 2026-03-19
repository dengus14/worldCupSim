package com.worldcupsim.demo.dto;

import com.worldcupsim.demo.enums.Position;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStateDTO {
    private Long id;
    private String name;
    private String teamId;
    private Position position;
    private Double x;
    private Double y;
    private Boolean hasBall;
}
