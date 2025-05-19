package com.ginkgooai.legalcase.exception;

/**
 * Exception thrown when there is an error recording form values
 */
public class FormValueRecordingException extends RuntimeException {

	public FormValueRecordingException(String message) {
		super(message);
	}

	public FormValueRecordingException(String message, Throwable cause) {
		super(message, cause);
	}

}