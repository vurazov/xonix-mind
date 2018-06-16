package com.lineate.xonix.mind.repositories;


import com.lineate.xonix.mind.domain.BotMatchDb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BotMatchRepository extends JpaRepository<BotMatchDb, Integer> {

    List<BotMatchDb> findByMatchId(Integer matchId);
}
