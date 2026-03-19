package com.worldcupsim.demo.model;

import com.worldcupsim.demo.model.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    @Id
    private String id;

    @ManyToOne
    private Team homeTeam;

    @ManyToOne
    private Team awayTeam;

    private Integer homeScore = 0;
    private Integer awayScore = 0;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @CreatedDate
    private LocalDateTime createdAt;
}
