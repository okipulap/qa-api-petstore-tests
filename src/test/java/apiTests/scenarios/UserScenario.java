package apiTests.scenarios;

import apiTests.base.UserClient;
import apiTests.factories.UserFactory;
import apiTests.models.user.User;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.junit.jupiter.api.*;

@Epic("Petstore API: Пользовательские сценарии")
@Owner("Nikita Tkachenko")
public class UserScenario {
	private static UserClient client;

	@BeforeAll
	public static void setUp() {
		client = new UserClient();
	}

//	@Test
//	@Tag("Positive")
//	@Tag("E2E")
//	@DisplayName("E2E-сценарий: жизненный цикл пользователя")
//	@Feature("Пользователь")
//	void userScenario() {
//		//Создание пользователя
//		User userPostRequest = UserFactory.randomUser();
//		User userPostResponse = client.postUser(userPostRequest);
//
//		//Получение пользователя
//		User userGetResponse =
//			client.getUserByUsername(userPostResponse.getUsername());
//
//		User userPutRequest = UserFactory.updateUser(userPostResponse.getId(), userPostResponse.getUsername())
//	}
}
