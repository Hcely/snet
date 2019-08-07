package com.snet.buffer.exception;

public class SNetBufferException extends RuntimeException {
	private static final long serialVersionUID = -8057000384969572988L;

	public SNetBufferException(String message) {
		super(message);
	}

	public SNetBufferException(String message, Throwable cause) {
		super(message, cause);
	}
}
