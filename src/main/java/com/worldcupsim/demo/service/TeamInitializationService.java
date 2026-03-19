package com.worldcupsim.demo.service;

import com.worldcupsim.demo.enums.Position;
import com.worldcupsim.demo.model.BehaviorWeights;
import com.worldcupsim.demo.model.Player;
import com.worldcupsim.demo.model.Team;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class TeamInitializationService {

    private final Random random = new Random();

    public Team createFranceTeam() {
        Team team = new Team();
        team.setName("France");
        team.setCountry("FRA");
        team.setFormation("4-3-3");

        List<Player> players = new ArrayList<>();

        players.add(createPlayer("Maignan", team, Position.GK, 50, 340, "calm and reliable"));
        players.add(createPlayer("Koundé", team, Position.DEF, 200, 170, "fast and aggressive"));
        players.add(createPlayer("Varane", team, Position.DEF, 200, 290, "experienced leader"));
        players.add(createPlayer("Upamecano", team, Position.DEF, 200, 410, "physical defender"));
        players.add(createPlayer("Hernandez", team, Position.DEF, 200, 530, "attacking fullback"));
        players.add(createPlayer("Kanté", team, Position.MID, 400, 240, "tireless workhorse"));
        players.add(createPlayer("Tchouaméni", team, Position.MID, 400, 340, "defensive anchor"));
        players.add(createPlayer("Griezmann", team, Position.MID, 400, 440, "creative playmaker"));
        players.add(createPlayer("Dembélé", team, Position.FWD, 600, 170, "speedy winger"));
        players.add(createPlayer("Mbappé", team, Position.FWD, 600, 340, "world class striker"));
        players.add(createPlayer("Giroud", team, Position.FWD, 600, 510, "target man"));

        team.setPlayers(players);
        return team;
    }

    public Team createBrazilTeam() {
        Team team = new Team();
        team.setName("Brazil");
        team.setCountry("BRA");
        team.setFormation("4-3-3");

        List<Player> players = new ArrayList<>();

        players.add(createPlayer("Alisson", team, Position.GK, 1000, 340, "commanding presence"));
        players.add(createPlayer("Danilo", team, Position.DEF, 850, 170, "versatile defender"));
        players.add(createPlayer("Marquinhos", team, Position.DEF, 850, 290, "tactical leader"));
        players.add(createPlayer("Militão", team, Position.DEF, 850, 410, "strong tackler"));
        players.add(createPlayer("Telles", team, Position.DEF, 850, 530, "overlapping fullback"));
        players.add(createPlayer("Casemiro", team, Position.MID, 650, 240, "defensive rock"));
        players.add(createPlayer("Paquetá", team, Position.MID, 650, 340, "technical midfielder"));
        players.add(createPlayer("Bruno Guimarães", team, Position.MID, 650, 440, "box to box"));
        players.add(createPlayer("Raphinha", team, Position.FWD, 450, 170, "tricky winger"));
        players.add(createPlayer("Neymar", team, Position.FWD, 450, 340, "magical playmaker"));
        players.add(createPlayer("Vinícius Jr", team, Position.FWD, 450, 510, "explosive dribbler"));

        team.setPlayers(players);
        return team;
    }

    private Player createPlayer(String name, Team team, Position position, double x, double y, String personality) {
        Player player = new Player();
        player.setName(name);
        player.setTeam(team);
        player.setPosition(position);
        player.setX(x);
        player.setY(y);
        player.setPersonality(personality);

        player.setSpeed(randomAttribute(0.7, 0.95));
        player.setAccuracy(randomAttribute(0.7, 0.95));
        player.setAggression(randomAttribute(0.7, 0.95));
        player.setPassing(randomAttribute(0.7, 0.95));
        player.setShooting(randomAttribute(0.7, 0.95));

        BehaviorWeights weights = createBehaviorWeights(position);
        player.setBehaviorWeights(weights);

        return player;
    }

    private BehaviorWeights createBehaviorWeights(Position position) {
        switch (position) {
            case FWD:
                return new BehaviorWeights(
                    randomAttribute(0.6, 0.8),
                    randomAttribute(0.4, 0.6),
                    randomAttribute(0.5, 0.7),
                    randomAttribute(0.5, 0.7)
                );
            case MID:
                return new BehaviorWeights(
                    randomAttribute(0.3, 0.5),
                    randomAttribute(0.6, 0.8),
                    randomAttribute(0.4, 0.6),
                    randomAttribute(0.6, 0.8)
                );
            case DEF:
                return new BehaviorWeights(
                    randomAttribute(0.2, 0.4),
                    randomAttribute(0.5, 0.7),
                    randomAttribute(0.6, 0.8),
                    randomAttribute(0.6, 0.8)
                );
            case GK:
                return new BehaviorWeights(
                    randomAttribute(0.1, 0.2),
                    randomAttribute(0.3, 0.5),
                    randomAttribute(0.7, 0.9),
                    randomAttribute(0.8, 0.9)
                );
            default:
                return new BehaviorWeights(0.5, 0.5, 0.5, 0.5);
        }
    }

    private double randomAttribute(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
}
