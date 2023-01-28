package com.shailendra.circuitbreaker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.log4j.Logger;
/**
 * DriverClass Test showing usage of circuit breaker.
 */
class DriverClassTest {

	private static final Logger LOGGER = Logger.getLogger(DriverClassTest.class);

	// Startup delay for delayed service (in seconds)
	private static final int STARTUP_DELAY = 4;

	// Number of failed requests for circuit breaker to open
	private static final int FAILURE_THRESHOLD = 1;

	// Time period in seconds for circuit breaker to retry service
	private static final int RETRY_PERIOD = 2;

	private MonitoringService monitoringService;

	private CircuitBreaker delayedServiceCircuitBreaker;

	private CircuitBreaker quickServiceCircuitBreaker;

	/**
	 * Setup the circuit breakers and services, where {@link ServiceWithSimulatedDelay}
	 * starting with a delay of 4 seconds and a {@link ServiceWithoutSimulatedDelay}
	 * responding healthy. Both services are wrapped in a
	 * {@link Foo} proxy implementation with failure threshold of 1
	 * failure and retry time period of 2 seconds.
	 */
	@BeforeEach
	void setupCircuitBreakers() {
		var delayedService = new ServiceWithSimulatedDelay(System.nanoTime(), STARTUP_DELAY);
		// Set the circuit Breaker parameters
		delayedServiceCircuitBreaker = new Foo(delayedService, 3000, FAILURE_THRESHOLD,
				RETRY_PERIOD * 1000 * 1000 * 1000);

		var quickService = new ServiceWithoutSimulatedDelay();
		// Set the circuit Breaker parameters
		quickServiceCircuitBreaker = new Foo(quickService, 3000, FAILURE_THRESHOLD,
				RETRY_PERIOD * 1000 * 1000 * 1000);

		monitoringService = new MonitoringService(delayedServiceCircuitBreaker, quickServiceCircuitBreaker);

	}

	@Test
	void testFailure_OpenStateTransition() {
		// Calling delayed service, which will be unhealthy till 4 seconds
		assertEquals("ServiceWithSimulatedDelay is down", monitoringService.fetchDelayedServiceResponse());
		// As failure threshold is "1", the circuit breaker is changed to OPEN
		assertEquals("OPEN", delayedServiceCircuitBreaker.getState());
		// As circuit state is OPEN, we expect a quick fallback response from circuit
		// breaker.
		assertEquals("ServiceWithSimulatedDelay is down", monitoringService.fetchDelayedServiceResponse());

		// Meanwhile, the quick service is responding and the circuit state is CLOSED
		assertEquals("ServiceWithoutSimulatedDelay is working", monitoringService.fetchServiceResponse());
		assertEquals("CLOSED", quickServiceCircuitBreaker.getState());

	}

	@Test
	void testFailure_HalfOpenStateTransition() {
		// Calling delayed service, which will be unhealthy till 4 seconds
		assertEquals("ServiceWithSimulatedDelay is down", monitoringService.fetchDelayedServiceResponse());
		// As failure threshold is "1", the circuit breaker is changed to OPEN
		assertEquals("OPEN", delayedServiceCircuitBreaker.getState());

		// Waiting for recovery period of 2 seconds for circuit breaker to retry
		// service.
		try {
			LOGGER.info("Waiting 2s for ServiceWithSimulatedDelay to become responsive");
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// After 2 seconds, the circuit breaker should move to "HALF_OPEN" state and
		// retry fetching response from service again
		assertEquals("HALF_OPEN", delayedServiceCircuitBreaker.getState());

	}

	@Test
	void testRecovery_ClosedStateTransition() {
		// Calling delayed service, which will be unhealthy till 4 seconds
		assertEquals("ServiceWithSimulatedDelay is down", monitoringService.fetchDelayedServiceResponse());
		// As failure threshold is "1", the circuit breaker is changed to OPEN
		assertEquals("OPEN", delayedServiceCircuitBreaker.getState());

		// Waiting for 4 seconds, which is enough for DelayedService to become healthy
		// and respond successfully.
		try {
			LOGGER.info("Waiting 4s for delayed service to become responsive");
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// As retry period is 2 seconds (<4 seconds of wait), hence the circuit breaker
		// should be back in HALF_OPEN state.
		assertEquals("HALF_OPEN", delayedServiceCircuitBreaker.getState());
		// Check the success response from delayed service.
		assertEquals("ServiceWithSimulatedDelay is working", monitoringService.fetchDelayedServiceResponse());
		// As the response is success, the state should be CLOSED
		assertEquals("CLOSED", delayedServiceCircuitBreaker.getState());
	}

}