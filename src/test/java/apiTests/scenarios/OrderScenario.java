package apiTests.scenarios;

import apiTests.base.StoreClient;
import apiTests.factories.OrderFactory;
import apiTests.models.store.Order;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static apiTests.asserts.StoreAssertions.assertsStoreFieldsMatch;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.*;

@Epic("Petstore API: сценарии заказов")
@Owner("Nikita Tkachenko")
public class OrderScenario {

	private static StoreClient client;

	@BeforeAll
	public static void setUp() {
		client = new StoreClient();
	}

	@Test
	@Tag("Positive")
	@Tag("E2E")
	@DisplayName("E2E-сценарий: жизненный цикл заказа")
	@Feature("Заказ")
	void orderScenario() {
		// Получение количества заказов до создания
		int approvedBefore = client.getInventory().getApproved();

		// Создание заказа
		Order orderPostRequest = OrderFactory.randomOrder();
		Order orderPostResponse = client.postOrder(orderPostRequest);

		assertsStoreFieldsMatch(orderPostRequest, orderPostResponse);

		// Проверка изменения inventory
		int approvedAfter = client.getInventory().getApproved();

		assertThat(approvedAfter).isGreaterThan(approvedBefore);

		// Получение заказа
		Order orderGetResponse = client.getOrderById(orderPostResponse.getId());

		assertsStoreFieldsMatch(orderPostRequest, orderGetResponse);

		// Удаление заказа
		Response orderDeleteResponse =
			client.deleteOrder(orderGetResponse.getId());

		assertEquals(HttpStatus.SC_OK, orderDeleteResponse.getStatusCode());

		// Получение заказа после удаления
		Response getAfterDeleteResponse =
			client.getOrderExpected404(orderGetResponse.getId());

		assertEquals(HttpStatus.SC_NOT_FOUND, getAfterDeleteResponse.getStatusCode());
	}
}
