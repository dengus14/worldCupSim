package com.worldcupsim.demo.repository;

import com.worldcupsim.demo.model.GameEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameEventRepository extends JpaRepository<GameEvent, Long> {
}
