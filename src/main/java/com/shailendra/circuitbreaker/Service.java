package com.shailendra.circuitbreaker;

/**
 * The service interface that is implemented by services so that the services
 * can be called by {@link CircuitBreaker} without tight coupling.
 */
public interface Service {

	public String call() throws ServiceException;
}