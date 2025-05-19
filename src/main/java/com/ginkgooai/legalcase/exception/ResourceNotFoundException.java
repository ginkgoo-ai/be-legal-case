package com.ginkgooai.legalcase.exception;

/**
 * 资源未找到异常 Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}

	public ResourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}