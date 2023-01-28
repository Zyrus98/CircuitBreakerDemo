package com.shailendra.circuitbreaker;

/**
 * This simulates the remote service It responds only after a certain timeout
 * period (default set to 20 seconds).
 */
public class ServiceWithSimulatedDelay implements Service {
	private final long serverStartTime;
	private final int delay;


	public ServiceWithSimulatedDelay(long serverStartTime, int delay) {
		this.serverStartTime = serverStartTime;
		this.delay = delay;
	}

	public ServiceWithSimulatedDelay() {
		this.serverStartTime = System.nanoTime();
		this.delay = 20;
	}


	@Override
	public String call() throws ServiceException {
		var currentTime = System.nanoTime();
		if ((currentTime - serverStartTime) * 1.0 / (1000 * 1000 * 1000) < delay) {
			throw new ServiceException("ServiceWithSimulatedDelay is down");
		}
		return "ServiceWithSimulatedDelay is working";
	}
}
