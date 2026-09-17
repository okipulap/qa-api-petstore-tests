package apiTests.tests;

import apiTests.base.UserClient;
import apiTests.factories.UserFactory;
import apiTests.models.user.User;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Epic("PetStore API: пользователи")
@Owner("Nikita Tkachenko")
public class UserTests {

	private static UserClient client;
	private final List<String> createdUsernames = new ArrayList<>();
	private static final String FAKE_USERNAME = "test_username";

	@BeforeAll
	public static void setUp() {
		client = new UserClient();
	}

	private void assertUserFieldsMatch(User request, User response) {
		SoftAssertions soft = new SoftAssertions();

		soft.assertThat(request.getId()).isEqualTo(response.getId());
		soft.assertThat(request.getUsername()).isEqualTo(response.getUsername());
		soft.assertThat(request.getFirstName()).isEqualTo(response.getFirstName());
		soft.assertThat(request.getLastName()).isEqualTo(response.getLastName());
		soft.assertThat(request.getEmail()).isEqualTo(response.getEmail());
		soft.assertThat(request.getPassword()).isEqualTo(response.getPassword());
		soft.assertThat(request.getPhone()).isEqualTo(response.getPhone());
		soft.assertThat(request.getUserStatus()).isEqualTo(response.getUserStatus());
		soft.assertAll();
	}

	@Test
	@Tag("Positive")
	@Tag("Smoke")
	@DisplayName("Создание юзера")
	@Severity(SeverityLevel.BLOCKER)
	@Feature("Ручка API создания юзера")
	@Story("Юзер создает юзера")
	void postUserWithStatus200() {
		User request = UserFactory.randomUser();

		User response = client.postUser(request);

		createdUsernames.add(response.getUsername());

		assertUserFieldsMatch(request, response);
	}

	@Test
	@Tag("Positive")
	@Tag("Smoke")
	@DisplayName("Получение юзера")
	@Severity(SeverityLevel.BLOCKER)
	@Feature("Ручка API получения юзера")
	@Story("Юзер получает юзера")
	void getUserWithStatus200() {
		User request = UserFactory.randomUser();

		User response = client.postUser(request);
		User getResponse = client.getUserByUsername(response.getUsername());

		createdUsernames.add(getResponse.getUsername());

		assertNotNull(getResponse);
		assertUserFieldsMatch(request, getResponse);
	}

	@Test
	@Tag("Positive")
	@DisplayName("Создание списка юзеров")
	@Severity(SeverityLevel.CRITICAL)
	@Feature("Ручка API создания списка юзеров")
	@Story("Юзер создает список юзеров")
	void postUserWithListStatus200() {
		List<User> request = UserFactory.listOfUsers(2);
		createdUsernames.add(request.get(0).getUsername());
		createdUsernames.add(request.get(1).getUsername());

		List<User> response = client.postUsersWithList(request);

		assertNotNull(response);
		assertEquals(2, response.size());

		User getFirst = client.getUserByUsername(request.get(0).getUsername());
		User getSecond = client.getUserByUsername(request.get(1).getUsername());

		assertEquals(request.get(0).getUsername(), getFirst.getUsername());
		assertEquals(request.get(0).getEmail(), getFirst.getEmail());

		assertEquals(request.get(1).getUsername(), getSecond.getUsername());
		assertEquals(request.get(1).getEmail(), getSecond.getEmail());

	}

	@Test
	@Tag("Negative")
	@DisplayName("Получение юзера по несуществующему username")
	@Severity(SeverityLevel.CRITICAL)
	@Feature("Ручка API получения юзера")
	@Story("Юзер получает юзера")
	void getUserExpected404() {
		Response getResponse = client.getUserExpected404(FAKE_USERNAME);


		assertEquals("User not found", getResponse.asString());
	}

	@Test
	@Tag("Positive")
	@Tag("Smoke")
	@DisplayName("Изменение юзера по его username")
	@Severity(SeverityLevel.BLOCKER)
	@Feature("Ручка API изменения юзера")
	@Story("Юзер изменяет юзера")
	void putUserWithStatus200() {
		User postRequest = UserFactory.randomUser();
		client.postUser(postRequest);

		User putRequest = UserFactory.updateUser(postRequest.getId(), postRequest.getUsername());
		User putResponse = client.putUserByUsername(putRequest, postRequest.getUsername());

		User getResponse = client.getUserByUsername(putResponse.getUsername());

		createdUsernames.add(putResponse.getUsername());

		assertNotNull(putResponse);
		assertUserFieldsMatch(putRequest, putResponse);
		assertUserFieldsMatch(putRequest, getResponse);
	}

	@Test
	@Tag("Negative")
	@DisplayName("Изменение юзера по несуществующему username")
	@Severity(SeverityLevel.CRITICAL)
	@Feature("Ручка API изменения юзера")
	@Story("Юзер изменяет несуществующего юзера")
	void putUserExpected404() {
		User putRequest = UserFactory.updateUser(1L, FAKE_USERNAME);

		Response putResponse = client.putUserByUsernameExpected404(putRequest, FAKE_USERNAME);

		assertEquals("User not found", putResponse.asString());
	}


	@Test
	@Tag("Positive")
	@Tag("Smoke")
	@DisplayName("Тест удаления юзера")
	@Severity(SeverityLevel.BLOCKER)
	@Feature("Ручка API удаления юзера")
	@Story("Юзер удаляет юзера")
	void deleteUserWithStatus200() {
		User postRequest = UserFactory.randomUser();

		User postResponse = client.postUser(postRequest);

		Response delResponse = client.deleteUser(postResponse.getUsername());

		assertEquals(HttpStatus.SC_OK, delResponse.getStatusCode());
	}

	@Test
	@Tag("Negative")
	@Tag("Bug")
	@Issue("5")
	@DisplayName("Тест удаления юзера с несуществующим username")
	@Severity(SeverityLevel.CRITICAL)
	@Feature("Ручка API удаления юзера")
	@Story("Юзер удаляет юзера")
	@Description("""
		Тест проверяет, что DELETE /user/{username} с несуществующим username
		возвращает 404 Not Found, как задокументировано в OpenAPI-спецификации.
		На локальном docker-образе swaggerapi/petstore3:1.0.27 эндпоинт
		возвращает 200 OK вместо ожидаемого 404.
		""")
	void deleteUserExpected404() {
		Response delResponse = client.deleteUserExpected404(FAKE_USERNAME);

		assertEquals(HttpStatus.SC_NOT_FOUND, delResponse.getStatusCode());
	}

	@AfterEach
	void cleanUp() {
		for (String username : createdUsernames) {
			client.deleteUser(username);
		}
		createdUsernames.clear();
	}
}
