package com.hydra.core.entity.pk;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TeamCoachId implements Serializable {

	@Column(name = "team_id", length = 36)
	private String teamId;

	@Column(name = "coach_id", length = 36)
	private String coachId;

}