package apiTests.scenarios;

import apiTests.base.UserClient;
import apiTests.factories.UserFactory;
import apiTests.models.user.User;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
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
		// Создание пользователя
		User userPostRequest = UserFactory.randomUser();
		User userPostResponse = client.postUser(userPostRequest);
		assertUserFieldsMatch(userPostRequest, userPostResponse);

		// Получение пользователя
		User userGetResponse =
			client.getUserByUsername(userPostResponse.getUsername());
		assertUserFieldsMatch(userPostRequest, userGetResponse);

		// Изменение пользователя
		User userPutRequest =
			UserFactory.updateUser(userPostResponse.getId(), userPostResponse.getUsername());
		User userPutResponse = client.putUserByUsername(userPutRequest, userGetResponse.getUsername());
		assertUserFieldsMatch(userPutRequest, userPutResponse);

		// Получение после изменения
		User userGetAfterUpdateResponse =
			client.getUserByUsername(userPutResponse.getUsername());
		assertUserFieldsMatch(userPutRequest, userGetAfterUpdateResponse);

		// Удаление пользователя
		Response userDeleteResponse =
			client.deleteUser(userGetAfterUpdateResponse.getUsername());
		assertEquals(HttpStatus.SC_OK, userDeleteResponse.getStatusCode());

		// Получение пользователя после удаления
		Response getAfterDeleteResponse =
			client.getUserExpected404(userGetAfterUpdateResponse.getUsername());
		assertEquals(HttpStatus.SC_NOT_FOUND, getAfterDeleteResponse.getStatusCode());
	}
}
