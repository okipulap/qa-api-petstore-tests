package apiTests.asserts;

import apiTests.models.user.User;
import org.assertj.core.api.SoftAssertions;

public class UserAssertions {

	private UserAssertions() {

	}

	public static void assertUserFieldsMatch(User expected, User actual) {
		SoftAssertions soft = new SoftAssertions();

		soft.assertThat(actual.getId()).isEqualTo(expected.getId());
		soft.assertThat(actual.getUsername()).isEqualTo(expected.getUsername());
		soft.assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName());
		soft.assertThat(actual.getLastName()).isEqualTo(expected.getLastName());
		soft.assertThat(actual.getEmail()).isEqualTo(expected.getEmail());
		soft.assertThat(actual.getPassword()).isEqualTo(expected.getPassword());
		soft.assertThat(actual.getPhone()).isEqualTo(expected.getPhone());
		soft.assertThat(actual.getUserStatus()).isEqualTo(expected.getUserStatus());
		soft.assertAll();
	}
}
