package com.shailendra.circuitbreaker;

public interface CircuitBreaker {

	// called when request is successfully executed
	void recordSuccess();

	// called when request fails to execute
	void recordFailure(String response);

	// Get the current state of circuit breaker
	String getState();

	// Set the circuit breaker state manually.
	void setState(State state);

	// Attempt to fetch response from the remote service.
	String attemptRequest() throws ServiceException;
}
