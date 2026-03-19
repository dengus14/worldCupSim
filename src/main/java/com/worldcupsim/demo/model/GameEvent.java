package com.worldcupsim.demo.model;

import com.worldcupsim.demo.model.enums.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Match match;

    @Enumerated(EnumType.STRING)
    private EventType type;

    private Long playerId;
    private Long teamId;
    private Integer minute;
    private Double x;
    private Double y;
    private String description;

    private LocalDateTime timestamp;
}
