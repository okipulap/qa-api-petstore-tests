package apiTests.asserts;

import apiTests.models.store.Order;
import org.assertj.core.api.SoftAssertions;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public class StoreAssertions {

	private StoreAssertions() {

	}

	public static void assertsStoreFieldsMatch(Order actual, Order expected) {
		SoftAssertions soft = new SoftAssertions();

		OffsetDateTime requestTime = OffsetDateTime.parse(actual.getShipDate()).truncatedTo(ChronoUnit.MILLIS);
		OffsetDateTime responseTime = OffsetDateTime.parse(expected.getShipDate()).truncatedTo(ChronoUnit.MILLIS);

		soft.assertThat(actual.getId()).isEqualTo(expected.getId());
		soft.assertThat(actual.getPetId()).isEqualTo(expected.getPetId());
		soft.assertThat(actual.getQuantity()).isEqualTo(expected.getQuantity());
		soft.assertThat(requestTime).isEqualTo(responseTime);
		soft.assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
		soft.assertThat(actual.isComplete()).isEqualTo(expected.isComplete());
		soft.assertAll();
	}
}
