package apiTests.scenarios;

import apiTests.base.PetClient;
import apiTests.factories.PetFactory;
import apiTests.models.pet.Pet;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static apiTests.asserts.PetAssertions.assertPetFieldsMatch;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("Petstore API: сценарии питомцев")
@Owner("Nikita Tkachenko")
public class PetScenario {
	private static PetClient client;

	@BeforeAll
	public static void setUp() {
		client = new PetClient();
	}

	@Test
	@Tag("Positive")
	@Tag("E2E")
	@DisplayName("E2E-сценарий: жизненный цикл питомца")
	@Feature("Питомец")
	void petScenario() {
		// Создание питомца
		Pet petPostRequest = PetFactory.createPet("available");
		Pet petPostResponse = client.createPet(petPostRequest);
		assertPetFieldsMatch(petPostRequest, petPostResponse);

		// Получение питомца
		Pet petGetResponse = client.getPetById(petPostResponse.getId());
		assertPetFieldsMatch(petPostResponse, petGetResponse);

		// Изменение питомца
		Pet petPutRequest =
			PetFactory.updatePet(
				petPostResponse.getId(), "Updated " + petPostResponse.getName(), "sold");
		Pet petPutResponse = client.putPet(petPutRequest);
		assertPetFieldsMatch(petPutRequest, petPutResponse);

		// Получение после изменения
		Pet petGetAfterUpdateResponse = client.getPetById(petPutResponse.getId());
		assertPetFieldsMatch(petPutRequest, petGetAfterUpdateResponse);

		// Удаление питомца
		Response petDeleteResponse =
			client.deletePet(petGetAfterUpdateResponse.getId());
		assertEquals(HttpStatus.SC_OK, petDeleteResponse.getStatusCode());

		// Получение питомца после удаления
		Response getAfterDeleteResponse =
			client.getPetExpected404(petGetAfterUpdateResponse.getId());
		assertEquals(HttpStatus.SC_NOT_FOUND, getAfterDeleteResponse.getStatusCode());
	}
}
