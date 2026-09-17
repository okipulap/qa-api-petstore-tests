package apiTests.scenarios;

import apiTests.base.PetClient;
import apiTests.factories.PetFactory;
import apiTests.models.pet.Pet;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
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
		Pet petPostRequest = PetFactory.createPet("available");
		Pet petPostResponse = createPet(petPostRequest);

		getPetById(petPostResponse.getId(), petPostResponse);

		Pet petPutRequest =
			PetFactory.updatePet(
				petPostResponse.getId(), "Updated " + petPostResponse.getName(), "sold");
		Pet petPutResponse = updatePet(petPutRequest);

		getPetById(petPutResponse.getId(), petPutRequest);

		deletePet(petPutResponse.getId());

		getPetExpected404(petPutResponse.getId());
	}

	@Step("Создание питомца")
	private Pet createPet(Pet petPostRequest) {
		Pet petPostResponse = client.createPet(petPostRequest);
		assertPetFieldsMatch(petPostRequest, petPostResponse);
		return petPostResponse;
	}

	@Step("Получение созданного питомца по ID: {id}")
	private Pet getPetById(Long id, Pet expected) {
		Pet petGetResponse = client.getPetById(id);
		assertPetFieldsMatch(expected, petGetResponse);
		return petGetResponse;
	}

	@Step("Изменение питомца")
	private Pet updatePet(Pet petPutRequest) {
		Pet petPutResponse = client.putPet(petPutRequest);
		assertPetFieldsMatch(petPutRequest, petPutResponse);
		return petPutResponse;
	}

	@Step("Удаление питомца по ID: {id}")
	private void deletePet(Long id) {
		Response petDeleteResponse = client.deletePet(id);
		assertEquals(HttpStatus.SC_OK, petDeleteResponse.getStatusCode());
	}

	@Step("Проверка отсутствия питомца после удаления по ID: {id}")
	private void getPetExpected404(Long id) {
		Response getAfterDeleteResponse = client.getPetExpected404(id);
		assertEquals(HttpStatus.SC_NOT_FOUND, getAfterDeleteResponse.getStatusCode());
	}
}
