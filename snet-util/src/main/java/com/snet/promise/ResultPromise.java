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
		executeListeners(null);
		return true;
	}

	public boolean fail() {
		return fail(0);
	}

	public boolean fail(int code) {
		return fail(0, null);
	}

	public boolean fail(int code, String messsage) {
		return fail(0, null, null);
	}

	public boolean fail(int code, String messsage, Throwable cause) {
		if (!casState(INIT, COMPLETING))
			return false;
		this.statusCode = code;
		this.message = messsage;
		this.cause = cause;
		state = FAIL;
		executeListeners(null);
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
