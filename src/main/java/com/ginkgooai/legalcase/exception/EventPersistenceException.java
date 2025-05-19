package com.ginkgooai.legalcase.exception;

/**
 * Exception thrown when there is an error persisting an event
 */
public class EventPersistenceException extends RuntimeException {

	public EventPersistenceException(String message) {
		super(message);
	}

	public EventPersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

}