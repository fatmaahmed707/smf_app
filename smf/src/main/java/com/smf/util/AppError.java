package com.smf.util;

import org.springframework.http.HttpStatus;

public class AppError extends RuntimeException {
	private final HttpStatus status;
	private final String message;

	public AppError(HttpStatus status, String message) {
		super(message);
		this.status = status;
		this.message = message;
	}

	public HttpStatus getStatus() {
		return status;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
