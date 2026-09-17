package apiTests.scenarios;

import apiTests.base.UserClient;
import apiTests.factories.UserFactory;
import apiTests.models.user.User;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;

import static apiTests.asserts.UserAssertions.assertUserFieldsMatch;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Petstore API: пользовательские сценарии")
@Owner("Nikita Tkachenko")
public class UserScenario {
	private static UserClient client;

	@BeforeAll
	public static void setUp() {
		client = new UserClient();
	}

	@Test
	@Tag("Positive")
	@Tag("E2E")
	@DisplayName("E2E-сценарий: жизненный цикл пользователя")
	@Feature("Пользователь")
	void userScenario() {
		User userPostRequest = UserFactory.randomUser();
		User userPostResponse = createUser(userPostRequest);

		getUserByUsername(userPostResponse.getUsername(), userPostRequest);

		User userPutRequest =
			UserFactory.updateUser(userPostResponse.getId(), userPostResponse.getUsername());
		User userPutResponse = updateUser(userPutRequest, userPutRequest.getUsername());

		getUserByUsername(userPutResponse.getUsername(), userPutRequest);

		deleteUser(userPutResponse.getUsername());

		getUserExpected404(userPutResponse.getUsername());
	}

	@Step("Создание пользователя")
	private User createUser(User userPostRequest) {
		User userPostResponse = client.postUser(userPostRequest);
		assertUserFieldsMatch(userPostRequest, userPostResponse);
		return userPostResponse;
	}

	@Step("Получение пользователя по username: {username}")
	private User getUserByUsername(String username, User expected) {
		User userGetResponse = client.getUserByUsername(username);
		assertUserFieldsMatch(expected, userGetResponse);
		return userGetResponse;
	}

	@Step("Изменение пользователя по username: {username}")
	private User updateUser(User userPutRequest, String username) {
		User userPutResponse = client.putUserByUsername(userPutRequest, username);
		assertUserFieldsMatch(userPutRequest, userPutResponse);
		return userPutResponse;
	}

	@Step("Удаление пользователя по username: {username}")
	private void deleteUser(String username) {
		Response userDeleteResponse = client.deleteUser(username);
		assertEquals(HttpStatus.SC_OK, userDeleteResponse.getStatusCode());
	}

	@Step("Проверка отсутствия пользователя после удаления по username: {username}")
	private void getUserExpected404(String username) {
		Response getAfterDeleteResponse = client.getUserExpected404(username);
		assertEquals(HttpStatus.SC_NOT_FOUND, getAfterDeleteResponse.getStatusCode());
	}
}
