package apiTests.tests;

import apiTests.base.StoreClient;
import apiTests.factories.OrderFactory;
import apiTests.models.store.Order;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static apiTests.asserts.StoreAssertions.assertsStoreFieldsMatch;

@Epic("PetStore API: магазин")
@Owner("Nikita Tkachenko")
public class StoreTests {
	private static StoreClient client;
	private final List<Long> createdOrders = new ArrayList<>();
	private static final Long FAKE_ID = 9999L;

	@BeforeAll
	public static void setUp() {
		client = new StoreClient();
	}

	@Test
	@Tag("Positive")
	@DisplayName("Показ роста счетчика после создания заказа")
	@Severity(SeverityLevel.CRITICAL)
	@Feature("Ручка API получения статусов заказа")
	@Story("Юзер получает количества заказов по статусам")
	void getInventoryTestWithStatus200() {
		int approvedBefore = client.getInventory().getApproved();

		client.postOrder(OrderFactory.randomOrder());

		int approvedAfter = client.getInventory().getApproved();

		assertThat(approvedAfter).isGreaterThan(approvedBefore);
	}

	@Test
	@Tag("Positive")
	@Tag("Smoke")
	@DisplayName("Создание заказа")
	@Severity(SeverityLevel.BLOCKER)
	@Feature("Ручка API создания заказа")
	@Story("Юзер создает заказ")
	void postOrderWithStatus200() {
		Order request = OrderFactory.randomOrder();

		Order response = client.postOrder(request);
		createdOrders.add(response.getId());

		assertsStoreFieldsMatch(request, response);
	}

	@Test
	@Tag("Positive")
	@Tag("Smoke")
	@DisplayName("Получение заказа")
	@Severity(SeverityLevel.BLOCKER)
	@Feature("Ручка API Получения заказа")
	@Story("Юзер получает заказ")
	void getOrderWithStatus200() {
		Order postRequest = OrderFactory.randomOrder();
		Order postResponse = client.postOrder(postRequest);

		Order getResponse = client.getOrderById(postResponse.getId());
		createdOrders.add(getResponse.getId());

		assertsStoreFieldsMatch(postRequest, getResponse);
	}

	@Test
	@Tag("Negative")
	@DisplayName("Проверка статуса 404 при ненахождении заказа")
	@Severity(SeverityLevel.CRITICAL)
	@Feature("Ручка API выборки заказа")
	@Story("Юзер получает заказ")
	void getOrderTestWithStatus404() {
		Response response = client.getOrderExpected404(FAKE_ID);

		assertEquals("Order not found", response.asString());
	}

	@Test
	@Tag("Positive")
	@Tag("Smoke")
	@DisplayName("Удаление заказа")
	@Severity(SeverityLevel.BLOCKER)
	@Feature("Ручка API Удаление заказа")
	@Story("Юзер удаляет заказ")
	void deleteOrderWithStatus200() {
		Order postRequest = OrderFactory.randomOrder();
		Order postResponse = client.postOrder(postRequest);

		Response delResponse = client.deleteOrder(postResponse.getId());

		assertEquals(HttpStatus.SC_OK, delResponse.getStatusCode());
	}

	@Test
	@Tag("Negative")
	@Tag("Bug")
	@Issue("4")
	@DisplayName("Удаление несуществующего заказа")
	@Severity(SeverityLevel.CRITICAL)
	@Feature("Ручка API Удаление заказа")
	@Story("Юзер удаляет заказ")
	@Description("""
        Тест проверяет, что DELETE /store/order/{orderId} с несуществующим id
        возвращает 404 Not Found, как задокументировано в OpenAPI-спецификации.
        На локальном docker-образе swaggerapi/petstore3:1.0.27 эндпоинт
        возвращает 200 OK вместо ожидаемого 404.
		""")
	void deleteOrderExpected404() {
		Response delResponse = client.deleteOrderExpected404(FAKE_ID);

		assertEquals(HttpStatus.SC_NOT_FOUND, delResponse.getStatusCode());
	}

	@AfterEach
	void cleanUp() {
		for (Long orderId : createdOrders) {
			client.deleteOrder(orderId);
		}
		createdOrders.clear();
	}
}
