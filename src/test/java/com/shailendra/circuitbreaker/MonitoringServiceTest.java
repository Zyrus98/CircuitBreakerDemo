package com.shailendra.circuitbreaker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Monitoring Service test
 */
class MonitoringServiceTest {


	@Test
	void testDelayedRemoteResponseSuccess() {
		var delayedService = new ServiceWithSimulatedDelay(System.nanoTime() - 2 * 1000 * 1000 * 1000, 2);
		var delayedServiceCircuitBreaker = new Foo(delayedService, 3000, 1, 2 * 1000 * 1000 * 1000);

		var monitoringService = new MonitoringService(delayedServiceCircuitBreaker, null);
		// Set time in past to make the server work
		var response = monitoringService.fetchDelayedServiceResponse();
		assertEquals(response, "ServiceWithSimulatedDelay is working");
	}

	@Test
	void testDelayedRemoteResponseFailure() {
		var delayedService = new ServiceWithSimulatedDelay(System.nanoTime(), 2);
		var delayedServiceCircuitBreaker = new Foo(delayedService, 3000, 1, 2 * 1000 * 1000 * 1000);
		var monitoringService = new MonitoringService(delayedServiceCircuitBreaker, null);
		// Set time as current time as initially server fails
		var response = monitoringService.fetchDelayedServiceResponse();
		assertEquals(response, "ServiceWithSimulatedDelay is down");
	}

	@Test
	void testServiceWithoutSimulatedDelayResponse() {
		var delayedService = new ServiceWithoutSimulatedDelay();
		var delayedServiceCircuitBreaker = new Foo(delayedService, 3000, 1, 2 * 1000 * 1000 * 1000);
		var monitoringService = new MonitoringService(delayedServiceCircuitBreaker, null);
		// Set time as current time as initially server fails
		var response = monitoringService.fetchDelayedServiceResponse();
		assertEquals(response, "Quick Service is working");
	}
}