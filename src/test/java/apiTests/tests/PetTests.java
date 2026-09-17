package apiTests.tests;

import apiTests.base.PetClient;
import apiTests.factories.PetFactory;
import apiTests.models.ApiResponse;
import apiTests.models.*;
import apiTests.models.pet.Pet;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Epic("PetStore API: питомцы")
@Owner("Nikita Tkachenko")
public class PetTests {
	private static PetClient client;
	private final List<Long> createdPets = new ArrayList<>();
	private static final Long FAKE_ID = 9999L;

	@BeforeAll
	public static void setUp() {
		client = new PetClient();
	}

	@Step("Проверка полей питомца")
	private void assertPetFieldsMatch(Pet request, Pet response) {
		SoftAssertions soft = new SoftAssertions();
		soft.assertThat(request.getId())
			.isEqualTo(response.getId());

		soft.assertThat(request.getName())
			.isEqualTo(response.getName());

		soft.assertThat(request.getCategory())
			.usingRecursiveComparison()
			.isEqualTo(response.getCategory());

		soft.assertThat(request.getTags())
			.usingRecursiveComparison()
			.isEqualTo(response.getTags());

		soft.assertThat(request.getPhotoUrls())
			.isNotEmpty()
			.allMatch(url -> url.startsWith("https"));

		soft.assertAll();
	}

	private static Stream<Arguments> invalidPetRequests() {
		return Stream.of(
			Arguments.of("Битый JSON (id без значения)",
				"""
					{
					  "id": ,
					  "name": "doggie",
					  "category": {
					    "id": 1,
					    "name": "Dogs"
					  },
					  "tags": [
					    {
					      "id": 0,
					      "name": "string"
					    }
					  ],
					  "status": "available"
					}
					"""),
			Arguments.of("Битый JSON (нет закрывающей скобки)",
				"""
					{
					  "id": 123,
					  "name": "doggie"
					""")
		);
	}

	@Test
	@Tag("Positive")
	@Tag("Smoke")
	@DisplayName("Проверка создания питомца")
	@Severity(SeverityLevel.BLOCKER)
	@Feature("Ручка API добавления питомца")
	@Story("Юзер создает питомца")
	void postPetWithStatus200() {
		Pet request = PetFactory.createPet("available");

		Pet response = client.createPet(request);
		createdPets.add(response.getId());

		assertPetFieldsMatch(request, response);
	}

	@Test
	@Tag("Positive")
	@DisplayName("Проверка изменения питомца с помощью формы")
	@Severity(SeverityLevel.CRITICAL)
	@Feature("Ручка API изменения питомца с помощью формы")
	@Story("Юзер создает изменения с помощью формы")
	void updateWithFormDataTest() {
		Pet postRequest = PetFactory.createPet("available");
		client.createPet(postRequest);


		Pet formDataRequest = new Pet();
		formDataRequest.setId(postRequest.getId());
		formDataRequest.setName("form data name");
		formDataRequest.setStatus("pending");

		Pet formDataResponse = client.updatePetWithFormData(
			formDataRequest.getId(),
			formDataRequest.getName(), formDataRequest.getStatus());

		createdPets.add(formDataResponse.getId());

		assertNotNull(formDataResponse);
		assertEquals(formDataRequest.getName(), formDataResponse.getName());
		assertEquals(formDataRequest.getStatus(), formDataResponse.getStatus());
	}

	@Test
	@Tag("Negative")
	@DisplayName("Проверка изменения несуществующего питомца с помощью формы")
	@Severity(SeverityLevel.NORMAL)
	@Feature("Ручка API изменения питомца с помощью формы")
	@Story("Юзер создает питомца с помощью формы")
	void updateWithFormDataExpected404Test() {
		Pet formDataRequest = new Pet();
		formDataRequest.setId(FAKE_ID);
		formDataRequest.setName("form data name");
		formDataRequest.setStatus("sold");

		Response formDataResponse = client.updatePetWithFormDataWithStatus404(
			formDataRequest.getId(),
			formDataRequest.getName(), formDataRequest.getStatus());

		assertNotNull(formDataResponse);
		assertEquals("Pet not found", formDataResponse.asString());
	}

	@Test
	@Tag("Positive")
	@Tag("Bug")
	@Issue("3")
	@DisplayName("Проверка загрузки изображения питомца")
	@Severity(SeverityLevel.CRITICAL)
	@Feature("Ручка API загрузки изображения питомца")
	@Story("Юзер загружает изображение питомца")
	@Description("""
		Тест выполнен на публичной версии Swagger Petstore:
		локальный docker-образ swaggerapi/petstore3:1.0.27 не обрабатывает
		application/octet-stream для этого эндпоинта и возвращает 500
		вместо ожидаемого 200.
		""")
	void uploadPetImageTestWithStatus200() throws Exception {
		Pet request = PetFactory.createPet("available");
		Pet response = client.createPet(request);

		File image = new File(getClass().getResource("/pet.jpg").toURI());

		try (InputStream inputStream = new FileInputStream(image)) {
			Allure.addAttachment(
				"Изображение питомца",
				"image/jpeg",
				inputStream,
				".jpg"
			);
		}

		ApiResponse uploadImageResponse = client.uploadPetImage(response.getId(), image);

		createdPets.add(response.getId());

		assertNotNull(uploadImageResponse);
		assertEquals(200, uploadImageResponse.getCode());
		assertTrue(uploadImageResponse.getMessage().contains("pet.jpg"));

	}

	@ParameterizedTest
	@MethodSource("invalidPetRequests")
	@Tag("Negative")
	@DisplayName("Проверка создания питомца с невалидными id, name")
	@Severity(SeverityLevel.NORMAL)
	@Feature("Ручка API добавления питомца")
	@Story("Юзер создает питомца")
	void createPetTestWithStatus400(String brokenJson) {
		ApiResponse response = client.createPetWithBrokenJson(brokenJson);

		assertNotNull(response);
		assertEquals(400, response.getCode());
		assertTrue(response.getMessage().contains("Input error: unable to convert input to"));
	}

	@Test
	@Tag("Positive")
	@Tag("Smoke")
	@DisplayName("Проверка выборки питомца")
	@Severity(SeverityLevel.BLOCKER)
	@Feature("Ручка API выборки питомца")
	@Story("Юзер получает питомца")
	void getPetTestWithStatusCode200() {
		Pet request = PetFactory.createPet("available");

		client.createPet(request);
		Pet getResponse = client.getPetById(request.getId());
		createdPets.add(getResponse.getId());

		assertNotNull(getResponse);
		assertPetFieldsMatch(request, getResponse);
	}

	@ParameterizedTest
	@CsvSource({"available",
		"pending",
		"sold"
	})
	@Tag("Positive")
	@DisplayName("Проверка выборки питомцев по статусу")
	@Severity(SeverityLevel.CRITICAL)
	@Feature("Ручка API выборки питомцев по статусу")
	@Story("Юзер получает питомцев по статусу")
	void getPetByStatusTestWith200(String status) {
		List<Pet> petResponses = client.getPetByStatus(status);

		assertFalse(petResponses.isEmpty());
		assertThat(petResponses)
			.allMatch(p -> status.equals(p.getStatus()));
	}

	@Test
	@Tag("Negative")
	@DisplayName("Проверка статуса 400 при невалидном статусе")
	@Severity(SeverityLevel.NORMAL)
	@Feature("Ручка API выборки питомцев по статусу")
	@Story("Юзер получает питомцев по статусу")
	void getPetByStatusWith400() {
		ApiResponse petResponse = client.getPetByStatusExpected400("someStatus");

		assertNotNull(petResponse);
		assertEquals(400, petResponse.getCode());
		assertTrue(petResponse.getMessage().contains("Input error: query parameter `status value "));
	}

	@ParameterizedTest
	@CsvSource({"tag1",
		"tag2",
		"tag3"
	})
	@Tag("Positive")
	@DisplayName("Проверка выборки питомцев по тэгу")
	@Severity(SeverityLevel.CRITICAL)
	@Feature("Ручка API выборки питомцев по тэгу")
	@Story("Юзер получает питомцев по тэгу")
	void getPetByTagWithStatus200(String tag) {
		List<Pet> petResponse = client.getPetByTags(tag);

		assertFalse(petResponse.isEmpty());
		assertThat(petResponse)
			.allMatch(p -> p.getTags().stream()
				.anyMatch(t -> tag.equals(t.getName())));
	}

	@Test
	@Tag("Negative")
	@DisplayName("Проверка статуса 404 при ненахождении питомца")
	@Severity(SeverityLevel.NORMAL)
	@Feature("Ручка API выборки питомца")
	@Story("Юзер получает питомца")
	void getPetTestWithStatus404() {
		Response response = client.getPetExpected404(FAKE_ID);

		assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusCode());
		assertEquals("Pet not found", response.asString());
	}

	@ParameterizedTest
	@CsvSource({"pending",
		"sold"})
	@Tag("Positive")
	@DisplayName("Проверка изменения статуса питомца")
	@Severity(SeverityLevel.BLOCKER)
	@Feature("Ручка API изменения статуса питомца")
	@Story("Юзер изменяет статус питомца")
	void putPetTestWithStatus200(String status) {
		Pet postRequest = PetFactory.createPet("available");

		client.createPet(postRequest);

		Pet putRequest = PetFactory.updatePetFromExisting(
			postRequest.getId(), "putPet", status,
			postRequest.getPhotoUrls(), postRequest.getCategory(), postRequest.getTags());

		Pet putResponse = client.putPet(putRequest);
		Pet getResponse = client.getPetById(putRequest.getId());

		createdPets.add(getResponse.getId());

		assertPetFieldsMatch(putRequest, putResponse);
		assertPetFieldsMatch(putRequest, getResponse);
	}

	@Test
	@Tag("Negative")
	@DisplayName("Проверка изменения несуществующего питомца ")
	@Severity(SeverityLevel.NORMAL)
	@Feature("Ручка API изменения статуса питомца")
	@Story("Юзер изменяет статус питомца")
	void putPetExpected404() {
		Pet putRequest = PetFactory.updatePet(FAKE_ID, "putPet", "sold");

		Response putResponse = client.putPetExpected404(putRequest);

		assertEquals("Pet not found", putResponse.asString());
	}

	@Test
	@Tag("Positive")
	@Tag("Smoke")
	@DisplayName("Проверка удаления питомца")
	@Severity(SeverityLevel.BLOCKER)
	@Feature("Ручка API удаления питомца")
	@Story("Юзер удаляет питомца")
	void deletePetTestWithStatus200() {
		Pet postRequest = PetFactory.createPet("available");

		client.createPet(postRequest);

		Response response = client.deletePet(postRequest.getId());

		assertEquals(HttpStatus.SC_OK, response.getStatusCode());
		assertEquals("Pet deleted", response.asString());
	}

	@Test
	@Tag("Negative")
	@Tag("Bug")
	@Issue("2")
	@DisplayName("Проверка удаления питомца с несуществующим id")
	@Severity(SeverityLevel.CRITICAL)
	@Feature("Ручка API удаления питомца")
	@Story("Юзер удаляет питомца")
	@Description("""
		Тест выполнен на публичной версии Swagger Petstore:
		локальный docker-образ swaggerapi/petstore3:1.0.27 не соответствует спецификации
		DELETE-запроса к несуществующему питомцу.
		""")
	void deletePetTestWithStatus404() {
		Response response = client.deletePetExpected404(FAKE_ID);

		assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusCode());
	}

	@AfterEach
	void cleanUp() {
		for (Long petId : createdPets) {
			client.deletePet(petId);
		}
		createdPets.clear();
	}
}
