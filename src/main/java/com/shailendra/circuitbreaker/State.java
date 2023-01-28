package com.shailendra.circuitbreaker;

/**
 * Enumeration for states maintained by the circuit breaker.
 */
public enum State {
  CLOSED,
  OPEN,
  HALF_OPEN
}