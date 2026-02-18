package enums;

import com.hydra.core.enums.TeamRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class TeamRoleTest {

	@Test
	void owner_hasCorrectLabel() {
		assertThat(TeamRole.OWNER.getLabel()).isEqualTo("Dono");
	}

	@Test
	void coach_hasCorrectLabel() {
		assertThat(TeamRole.COACH.getLabel()).isEqualTo("Treinador");
	}

	@Test
	void athlete_hasCorrectLabel() {
		assertThat(TeamRole.ATHLETE.getLabel()).isEqualTo("Atleta");
	}

	@ParameterizedTest
	@ValueSource(strings = { "OWNER", "owner", "Owner", "oWnEr" })
	void fromString_withOwnerVariants_returnsOwner(String input) {
		assertThat(TeamRole.fromString(input)).isEqualTo(TeamRole.OWNER);
	}

	@ParameterizedTest
	@ValueSource(strings = { "COACH", "coach", "Coach", "cOaCh" })
	void fromString_withCoachVariants_returnsCoach(String input) {
		assertThat(TeamRole.fromString(input)).isEqualTo(TeamRole.COACH);
	}

	@ParameterizedTest
	@ValueSource(strings = { "ATHLETE", "athlete", "Athlete", "aThlEtE" })
	void fromString_withAthleteVariants_returnsAthlete(String input) {
		assertThat(TeamRole.fromString(input)).isEqualTo(TeamRole.ATHLETE);
	}

	@Test
	void fromString_withUnknownRole_returnsNull() {
		assertThat(TeamRole.fromString("ADMIN")).isNull();
	}

	@Test
	void fromString_withRandomString_returnsNull() {
		assertThat(TeamRole.fromString("xpto")).isNull();
	}

	@Test
	void values_containsExactlyThreeRoles() {
		assertThat(TeamRole.values()).containsExactly(TeamRole.OWNER, TeamRole.COACH, TeamRole.ATHLETE);
	}

	@Test
	void valueOf_returnsCorrectConstants() {
		assertThat(TeamRole.valueOf("OWNER")).isEqualTo(TeamRole.OWNER);
		assertThat(TeamRole.valueOf("COACH")).isEqualTo(TeamRole.COACH);
		assertThat(TeamRole.valueOf("ATHLETE")).isEqualTo(TeamRole.ATHLETE);
	}

}