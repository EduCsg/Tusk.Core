package com.hydra.core.repository;

import com.hydra.core.entity.TeamEntity;
import com.hydra.core.entity.TeamMemberEntity;
import com.hydra.core.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMemberEntity, String> {

	TeamMemberEntity findFirstByUserAndTeam(UserEntity user, TeamEntity team);

	Optional<TeamMemberEntity> findByTeamIdAndUserId(String teamId, String userId);

	TeamMemberEntity findFirstByUserOrderByJoinedAtAsc(UserEntity user);

	@Query(value = """
			SELECT * FROM team_members 
			WHERE team_id = :teamId 
			ORDER BY 
			    CASE role 
			        WHEN 'OWNER' THEN 1 
			        WHEN 'COACH' THEN 2 
			        WHEN 'ATHLETE' THEN 3 
			    END,
			    joined_at ASC
			""", nativeQuery = true)
	List<TeamMemberEntity> findAllByTeamOrderedByRole(@Param("teamId") String teamId);

}