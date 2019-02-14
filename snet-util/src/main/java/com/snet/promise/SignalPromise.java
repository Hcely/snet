package com.snet.promise;

public class SignalPromise extends AbstractPromise<SignalPromise> {
	protected static final int FINISH = 2;

	public boolean finish() {
		if (!casState(INIT, COMPLETING))
			return false;
		state = FINISH;
		executeListeners();
		return true;
	}

}
