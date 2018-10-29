package com.snet.data;

public interface RespResult {
	int OK_CODE = 200;

	int getStatusCode();

	String getMessage();

	boolean isSuccess();
}
