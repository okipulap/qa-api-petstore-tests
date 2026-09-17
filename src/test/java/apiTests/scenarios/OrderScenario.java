package apiTests.scenarios;

import apiTests.base.StoreClient;
import apiTests.factories.OrderFactory;
import apiTests.models.store.Order;
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
		int approvedBefore = getInventoryStatus();

		Order orderPostRequest = OrderFactory.randomOrder();
		Order orderPostResponse = createOrder(orderPostRequest);

		int approvedAfter = getInventoryStatus();

		assertInventoryIncreased(approvedBefore, approvedAfter);

		getOrderById(orderPostResponse.getId(), orderPostRequest);

		deleteOrder(orderPostResponse.getId());

		getOrderExpected404(orderPostResponse.getId());
	}

	@Step("Получение количества заказов до создания")
	private int getInventoryStatus() {
		return client.getInventory().getApproved();
	}

	@Step("Создание заказа")
	private Order createOrder(Order orderPostRequest) {
		Order orderPostResponse = client.postOrder(orderPostRequest);
		assertsStoreFieldsMatch(orderPostRequest, orderPostResponse);
		return orderPostResponse;
	}

	@Step("Проверка inventory: было {approvedBefore}, стало {approvedAfter}")
	private void assertInventoryIncreased(int approvedBefore, int approvedAfter) {
		assertThat(approvedAfter).isGreaterThan(approvedBefore);
	}

	@Step("Получение созданного заказа с ID: {id}")
	private Order getOrderById(Long id, Order expected) {
		Order orderGetResponse = client.getOrderById(id);
		assertsStoreFieldsMatch(expected, orderGetResponse);
		return orderGetResponse;
	}

	@Step("Удаление заказа с ID: {id}")
	private void deleteOrder(Long id) {
		Response orderDeleteResponse = client.deleteOrder(id);
		assertEquals(HttpStatus.SC_OK, orderDeleteResponse.getStatusCode());
	}

	@Step("Проверка отсутствия заказа после удаления с ID: {id}")
	private void getOrderExpected404(Long id) {
		Response getAfterDeleteResponse = client.getOrderExpected404(id);
		assertEquals(HttpStatus.SC_NOT_FOUND, getAfterDeleteResponse.getStatusCode());
	}
}
