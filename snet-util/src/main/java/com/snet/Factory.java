package com.snet;

public interface Factory<P, M> {
	P create(M object);
}
