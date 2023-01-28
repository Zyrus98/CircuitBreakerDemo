package com.shailendra.circuitbreaker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Circuit Breaker test
 */
class FooTest {

	@Test
	void testEvaluateState() {
		var circuitBreaker = new Foo(null, 1, 1, 100);
		// Right now, failureCount<failureThreshold, so state should be closed
		assertEquals(circuitBreaker.getState(), "CLOSED");
		circuitBreaker.failureCount = 4;
		circuitBreaker.lastFailureTime = System.nanoTime();
		circuitBreaker.evaluateState();
		// Since failureCount>failureThreshold, and lastFailureTime is nearly equal to
		// current time,
		// state should be half-open
		assertEquals(circuitBreaker.getState(), "HALF_OPEN");
		// Since failureCount>failureThreshold, and lastFailureTime is much lesser
		// current time,
		// state should be open
		circuitBreaker.lastFailureTime = System.nanoTime() - 1000 * 1000 * 1000 * 1000;
		circuitBreaker.evaluateState();
		assertEquals(circuitBreaker.getState(), "OPEN");
		// Now set it back again to closed to test idempotency
		circuitBreaker.failureCount = 0;
		circuitBreaker.evaluateState();
		assertEquals(circuitBreaker.getState(), "CLOSED");
	}

	@Test
	void testSetStateForBypass() {
		var circuitBreaker = new Foo(null, 1, 1, 2000 * 1000 * 1000);
		// Right now, failureCount<failureThreshold, so state should be closed
		// Bypass it and set it to open
		circuitBreaker.setState(State.OPEN);
		assertEquals(circuitBreaker.getState(), "OPEN");
	}

	@Test
	void testApiResponses() throws ServiceException {
		Service mockService = new Service() {
			@Override
			public String call() throws ServiceException {
				return "Remote Success";
			}
		};
		var circuitBreaker = new Foo(mockService, 1, 1, 100);
		// Call with the paramater start_time set to huge amount of time in past so that
		// service
		// replies with "Ok". Also, state is CLOSED in start
		var serviceStartTime = System.nanoTime() - 60 * 1000 * 1000 * 1000;
		var response = circuitBreaker.attemptRequest();
		assertEquals(response, "Remote Success");
	}
}