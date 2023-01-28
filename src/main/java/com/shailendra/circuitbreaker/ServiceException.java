package com.shailendra.circuitbreaker;

/**
 * Exception thrown when {@link a Service} does not respond successfully.
 */
public class ServiceException extends Exception {

	private static final long serialVersionUID = 8693921941400267993L;

	public ServiceException(String message) {
		super(message);
	}
}