package com.medallia.webcrawler.exception;

public class ApiException extends RuntimeException{
	private String errorMessage;
	public ApiException(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
