package com.shailendra.circuitbreaker;

/**
 * A quick response remote service, that responds healthy without any delay or
 * failure.
 */
public class ServiceWithoutSimulatedDelay implements Service {

	@Override
	public String call() throws ServiceException {
		return "ServiceWithoutSimulatedDelay is working";
	}
}