package com.lineate.xonix.mind.repositories;

import com.lineate.xonix.mind.domain.BotDb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotRepository extends JpaRepository<BotDb, Integer>{
}