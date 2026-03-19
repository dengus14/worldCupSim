package com.worldcupsim.demo.model;

import com.worldcupsim.demo.enums.Position;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    private Position position;

    private Double speed;
    private Double accuracy;
    private Double aggression;
    private Double passing;
    private Double shooting;

    @Embedded
    private BehaviorWeights behaviorWeights;

    private String personality;

    @Transient
    private Double x;

    @Transient
    private Double y;
}
