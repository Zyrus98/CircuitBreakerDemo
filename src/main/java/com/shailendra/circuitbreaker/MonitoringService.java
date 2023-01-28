package com.shailendra.circuitbreaker;

/**
 * The service class which makes local and remote calls Uses
 * {@link Foo } proxy to ensure remote calls don't use up
 * resources.
 */
public class MonitoringService {

	private final CircuitBreaker delayedService;

	private final CircuitBreaker quickService;

	public MonitoringService(CircuitBreaker delayedService, CircuitBreaker quickService) {
		this.delayedService = delayedService;
		this.quickService = quickService;
	}

	/**
	 * Fetch response from the delayed service (with some simulated startup time).
	 *
	 * @return response string
	 */
	public String fetchDelayedServiceResponse() {
		try {
			return this.delayedService.attemptRequest();
		} catch (ServiceException e) {
			return e.getMessage();
		}
	}

	/**
	 * Fetches response from a healthy service without any failure.
	 *
	 * @return response string
	 */
	public String fetchServiceResponse() {
		try {
			return this.quickService.attemptRequest();
		} catch (ServiceException e) {
			return e.getMessage();
		}
	}
}