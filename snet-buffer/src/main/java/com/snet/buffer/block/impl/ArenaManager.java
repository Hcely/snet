package com.snet.buffer.block.impl;

import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.resource.SNetBufferResourceFactory;
import com.snet.buffer.resource.SNetResource;
import com.snet.util.thread.Worker;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArenaManager {
	protected Timer monitor;
	protected Worker<SNetBlock> reclaimer;
	protected ConcurrentLinkedQueue<SNetBlockArena> arenas;
	protected SNetBufferResourceFactory resourceFactory;
	protected final int centerIdleTime;
	protected final int areaIdleTime;
	protected final int localIdleTime;

	public ArenaManager(int centerIdleTime, int areaIdleTime, int localIdleTime) {
		this.monitor = new Timer(true);
		this.reclaimer = new Worker<SNetBlock>(e -> e.getArena().recycle(e)).setDaemon(true);
		this.arenas = new ConcurrentLinkedQueue<>();
		this.centerIdleTime = centerIdleTime;
		this.areaIdleTime = areaIdleTime;
		this.localIdleTime = localIdleTime;
	}

	public void addArena(SNetBlockArena arena) {
		arenas.add(arena);
	}

	public SNetResource createResource(int capacity) {
		return resourceFactory.create(capacity);
	}

	public void recycleBlock(SNetBlock block) {
		reclaimer.execute(block);
	}

	public void recycleBlocks(List<SNetBlock> blocks) {
		reclaimer.execute(new RecycleRunner(blocks));
	}

	public int getCenterIdleTime() {
		return centerIdleTime;
	}

	public int getAreaIdleTime() {
		return areaIdleTime;
	}

	public int getLocalIdleTime() {
		return localIdleTime;
	}

	protected static class RecycleRunner implements Runnable {
		protected final List<SNetBlock> blocks;

		public RecycleRunner(List<SNetBlock> blocks) {
			this.blocks = blocks;
		}

		@Override
		public void run() {
			for (SNetBlock block : blocks)
				block.getArena().recycle(block);
		}
	}

}
