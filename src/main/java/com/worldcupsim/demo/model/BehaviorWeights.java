package com.worldcupsim.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorWeights {
    @Column(name = "behavior_shoot_tendency")
    private Double shootTendency;

    @Column(name = "behavior_pass_tendency")
    private Double passTendency;

    @Column(name = "behavior_aggression")
    private Double aggression;

    @Column(name = "behavior_positioning")
    private Double positioning;
}
