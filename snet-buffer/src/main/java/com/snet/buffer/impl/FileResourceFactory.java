package com.snet.buffer.impl;

import com.snet.buffer.SNetResource;
import com.snet.buffer.SNetResourceFactory;
import com.snet.buffer.SNetResourceManager;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

public class FileResourceFactory implements SNetResourceFactory {
	protected final File folder;
	protected final String prefixName;
	protected final AtomicLong idx;
	protected int channelLength = 4;

	public FileResourceFactory(String folderPath) {
		this(new File(folderPath));
	}

	public FileResourceFactory(String folderPath, String prefixName) {
		this(new File(folderPath), prefixName);
	}

	public FileResourceFactory(File folder) {
		this(folder, "buffer_resource");
	}

	public FileResourceFactory(File folder, String prefixName) {
		this.folder = folder;
		this.prefixName = prefixName;
		this.idx = new AtomicLong(0);
	}

	public int getChannelLength() {
		return channelLength;
	}

	public void setChannelLength(int channelLength) {
		this.channelLength = channelLength;
	}

	@Override
	public SNetResource create(SNetResourceManager manager, long capacity) {
		String fileName = nextName();
		File file = new File(folder, fileName);
		return new FileResource(manager, file, capacity, channelLength);
	}

	protected String nextName() {
		long nextIdx = idx.getAndIncrement();
		StringBuilder sb = new StringBuilder(prefixName);
		return sb.append('_').append(nextIdx).toString();
	}
}
