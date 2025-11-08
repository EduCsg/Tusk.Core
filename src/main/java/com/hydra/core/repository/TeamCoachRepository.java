package com.hydra.core.repository;

import com.hydra.core.entity.TeamCoachEntity;
import com.hydra.core.entity.pk.TeamCoachId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamCoachRepository extends JpaRepository<TeamCoachEntity, TeamCoachId> {

}