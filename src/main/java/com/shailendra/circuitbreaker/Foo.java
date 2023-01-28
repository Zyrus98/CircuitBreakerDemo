package com.shailendra.circuitbreaker;

/**
 * This class implements the {@link CircuitBreaker interface} and uses the delay
 * based circuit breaker strategy and maintains following three states using
 * {@link State enum } as: 
 * 
 * * CLOSED : service is in a fine state and can be called
 * * OPEN : service is unresponsive/ in failed state and cannot be called
 * * HALF_OPEN : current service state is unknown so can be called for confirmation
 */
public class Foo implements CircuitBreaker {

	private final long timeout;
	private final long retryTimeInterval;
	private final Service service;
	long lastFailureTime;
	private String lastFailureResponse;
	int failureCount;
	private final int failureThreshold;
	private State state;
	private final long futureTime = 1000L * 1000 * 1000 * 1000;

	public Foo(Service serviceToCall, long timeout, int failureThreshold, long retryTimeInterval) {

		// Timeout for service request to break the calls made to service if response time it exceeds the limit
		this.timeout = timeout;
		this.retryTimeInterval = retryTimeInterval;
		this.service = serviceToCall;
		this.failureThreshold = failureThreshold;
		// Initially allowing service calls
		this.state = State.CLOSED;
		// providing high value to indicate no failure occurred yet
		this.lastFailureTime = System.nanoTime() + futureTime;
	}

	// allow further calls to be made to service
	@Override
	public void recordSuccess() {
		this.failureCount = 0;
		this.lastFailureTime = System.nanoTime() + futureTime;
		this.state = State.CLOSED;
	}

	@Override
	public void recordFailure(String response) {
		failureCount = failureCount + 1;
		this.lastFailureTime = System.nanoTime();
		// store the failure response for returning on open state
		this.lastFailureResponse = response;
	}

	protected void evaluateState() {
		if (failureCount >= failureThreshold) { // indicated service call has failed
			if ((System.nanoTime() - lastFailureTime) > retryTimeInterval) {
				// after waiting for retryTimeInterval allowing service call for checking service state
				state = State.HALF_OPEN;
			} else {
				// Service would Still Probably be down if retryTimeInterval is not over
				state = State.OPEN;
			}
		} else {
			// Everything is working fine
			state = State.CLOSED;
		}
	}

	@Override
	public String getState() {
		evaluateState();
		return state.name();
	}

	/**
	 * manually set the service access by changing circuit breaker state
	 *
	 * @param state State at which circuit is in
	 */
	@Override
	public void setState(State state) {
		this.state = state;
		switch (state) {
		case OPEN:
			this.failureCount = failureThreshold;
			this.lastFailureTime = System.nanoTime();
			break;
		case HALF_OPEN:
			this.failureCount = failureThreshold;
			this.lastFailureTime = System.nanoTime() - retryTimeInterval;
			break;
		default:
			this.failureCount = 0;
			break;
		}

	}

	/**
	 * Executes service call.
	 *
	 * @return Value from the remote resource, recorded response or a custom exception
	 */
	@Override
	public String attemptRequest() throws ServiceException {
		evaluateState();
		if (state == State.OPEN) {
			// return stored failure response if the circuit is in Open state
			return this.lastFailureResponse;
		} else {
			// call service if the circuit is not OPEN
			try {
				var response = service.call();
				// call is successful
				recordSuccess();
				return response;
			} catch (ServiceException ex) {
				// call failed
				recordFailure(ex.getMessage());
				throw ex;
			}
		}
	}

}
