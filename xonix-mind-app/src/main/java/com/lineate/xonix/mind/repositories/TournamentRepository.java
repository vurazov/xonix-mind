package com.lineate.xonix.mind.repositories;


import com.lineate.xonix.mind.domain.TournamentDb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentRepository extends JpaRepository<TournamentDb, Integer> {
}
