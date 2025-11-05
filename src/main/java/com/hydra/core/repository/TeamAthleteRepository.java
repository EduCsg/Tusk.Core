package com.hydra.core.repository;

import com.hydra.core.entity.TeamAthleteEntity;
import com.hydra.core.entity.pk.TeamAthleteId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamAthleteRepository extends JpaRepository<TeamAthleteEntity, TeamAthleteId> {

}