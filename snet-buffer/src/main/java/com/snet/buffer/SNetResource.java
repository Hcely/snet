package com.snet.buffer;

import com.snet.Initializable;
import com.snet.Releasable;
import com.snet.SNetObject;
import com.snet.Slicable;

import java.nio.ByteBuffer;

public interface SNetResource extends SNetObject, Initializable, Releasable, Slicable<SNetResource> {

	SNetResourceManager getManager();

	SNetResource slice();

	Object getRawObject();

	long getCapacity();

	int write(long off, byte[] src, int srcOff, int srcLen);

	default int write(long off, ByteBuffer src) {
		return write(off, src, src.remaining());
	}

	int write(long off, ByteBuffer src, int srcLen);

	long write(long off, SNetResource src, long srcOff, long srcLen);

	int read(long off, byte[] dst, int dstOff, int dstLen);

	default int read(long off, ByteBuffer dst) {
		return read(off, dst, dst.remaining());
	}

	int read(long off, ByteBuffer dst, int dstLen);
}
