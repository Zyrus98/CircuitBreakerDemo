package com.shailendra.circuitbreaker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Monitoring Service test
 */
class DelayedServiceTest {

	/**
	 * Testing immediate response of the delayed service.
	 *
	 * @throws ServiceException
	 */
	@Test
	void testDefaultConstructor() throws ServiceException {
		Assertions.assertThrows(ServiceException.class, () -> {
			var obj = new ServiceWithSimulatedDelay();
			obj.call();
		});
	}

	/**
	 * Testing server started in past (2 seconds ago) and with a simulated delay of
	 * 1 second.
	 *
	 * @throws ServiceException
	 */
	@Test
	void testParameterizedConstructor() throws ServiceException {
		var obj = new ServiceWithSimulatedDelay(System.nanoTime() - 2000 * 1000 * 1000, 1);
		assertEquals("ServiceWithSimulatedDelay is working", obj.call());
	}
}