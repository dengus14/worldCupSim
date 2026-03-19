package com.worldcupsim.demo.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorWeights {
    private Double shootTendency;
    private Double passTendency;
    private Double aggression;
    private Double positioning;
}
