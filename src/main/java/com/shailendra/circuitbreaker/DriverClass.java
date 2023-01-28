package com.shailendra.circuitbreaker;

import org.apache.log4j.Logger;

/**
 * @author Shailendra Kumar Soni
 * 
 * This is the driver class to simulate the user behaviour.
 *
 */
public class DriverClass {

	private static final Logger LOGGER = Logger.getLogger(DriverClass.class);

	/**
	 * Driver Function
	 *
	 * @param args command line args
	 */
	public static void main(String[] args) {

		var serverStartTime = System.nanoTime();

		var delayedService = new ServiceWithSimulatedDelay(serverStartTime, 5);
		var delayedServiceCircuitBreaker = new Foo(delayedService, 3000, 2, 2000 * 1000 * 1000);

		var quickService = new ServiceWithoutSimulatedDelay();
		var quickServiceCircuitBreaker = new Foo(quickService, 3000, 2, 2000 * 1000 * 1000);

		// Create an object of monitoring service which makes service calls
		var monitoringService = new MonitoringService(delayedServiceCircuitBreaker, quickServiceCircuitBreaker);


		// Fetch response from delayed service 2 times, to meet the failure threshold
		LOGGER.info(monitoringService.fetchDelayedServiceResponse());
		LOGGER.info(monitoringService.fetchDelayedServiceResponse());

		// Fetch current state of delayed service circuit breaker after crossing failure
		// threshold limit which is OPEN now
		LOGGER.info(delayedServiceCircuitBreaker.getState());

		// Meanwhile, the delayed service is down, fetch response from the healthy quick service
		LOGGER.info(monitoringService.fetchServiceResponse());
		LOGGER.info(quickServiceCircuitBreaker.getState());

		// Wait for the delayed service to become responsive
		try {
			LOGGER.info("Waiting for delayed service to become responsive");
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Check the state of delayed circuit breaker, should be HALF_OPEN
		LOGGER.info(delayedServiceCircuitBreaker.getState());

		// Fetch response from delayed service, which should be healthy by now
		LOGGER.info(monitoringService.fetchDelayedServiceResponse());
		// As successful response is fetched, it should be CLOSED again.
		LOGGER.info(delayedServiceCircuitBreaker.getState());
	}
}