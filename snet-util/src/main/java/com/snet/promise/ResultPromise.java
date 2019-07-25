package com.snet.promise;

import com.snet.data.BasicResult;

public class ResultPromise<V> extends AbstractPromise<ResultPromise<V>> implements BasicResult<V> {
	protected static final int SUCC = 2;
	protected static final int FAIL = 3;

	protected int statusCode;
	protected String message;
	protected V result;
	protected Throwable cause;

	public boolean succ() {
		return succ(null);
	}

	public boolean succ(V result) {
		if (!casState(INIT, COMPLETING))
			return false;
		this.statusCode = OK_CODE;
		this.result = result;
		state = SUCC;
		executeListeners();
		return true;
	}

	public boolean fail() {
		return fail(0);
	}

	public boolean fail(int code) {
		return fail(code, null);
	}

	public boolean fail(int code, String message) {
		return fail(code, message, null);
	}

	public boolean fail(int code, String message, Throwable cause) {
		if (!casState(INIT, COMPLETING))
			return false;
		this.statusCode = code;
		this.message = message;
		this.cause = cause;
		state = FAIL;
		executeListeners();
		return true;
	}

	public boolean isSuccess() {
		return state == SUCC;
	}

	public boolean isFailure() {
		return state == FAIL;
	}

	@Override
	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public V getResult() {
		return result;
	}

	public Throwable getCause() {
		return cause;
	}

}
