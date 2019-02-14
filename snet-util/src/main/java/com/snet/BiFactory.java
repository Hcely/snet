package com.snet;

public interface BiFactory<P, M, N> {
	P create(M obj0, N obj1);
}
