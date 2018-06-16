package com.lineate.xonix.mind.repositories;

import com.lineate.xonix.mind.domain.MatchDb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<MatchDb, Integer>{
}