package apiTests.asserts;

import apiTests.models.pet.Pet;
import org.assertj.core.api.SoftAssertions;

public class PetAssertions {

	private PetAssertions() {

	}

	public static void assertPetFieldsMatch(Pet expected, Pet actual) {
		SoftAssertions soft = new SoftAssertions();

		soft.assertThat(actual.getId())
			.isEqualTo(expected.getId());

		soft.assertThat(actual.getName())
			.isEqualTo(expected.getName());

		soft.assertThat(actual.getCategory())
			.usingRecursiveComparison()
			.isEqualTo(expected.getCategory());

		soft.assertThat(actual.getTags())
			.usingRecursiveComparison()
			.isEqualTo(expected.getTags());

		soft.assertThat(actual.getPhotoUrls())
			.isNotEmpty()
			.allMatch(url -> url.startsWith("https"));

		soft.assertAll();
	}
}
