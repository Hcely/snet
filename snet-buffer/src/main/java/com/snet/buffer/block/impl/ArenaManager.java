package com.snet.buffer.block.impl;

import com.snet.Initializable;
import com.snet.Releasable;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockAllocator;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.resource.SNetBufferResourceFactory;
import com.snet.buffer.resource.SNetResource;
import com.snet.util.thread.Worker;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArenaManager implements SNetBlockAllocator, Initializable, Releasable {

	protected Timer monitor;
	protected Worker<SNetBlock> reclaimer;
	protected final ConcurrentLinkedQueue<SNetBlockArena> arenas;
	protected final SNetBufferResourceFactory resourceFactory;
	protected int centerIdleTime;
	protected int areaIdleTime;
	protected int localIdleTime;
	protected boolean released;

	public ArenaManager(SNetBufferResourceFactory resourceFactory) {
		this.arenas = new ConcurrentLinkedQueue<>();
		this.resourceFactory = resourceFactory;
		this.centerIdleTime = 10000;
		this.areaIdleTime = 5000;
		this.localIdleTime = 2500;
	}

	public void setCenterIdleTime(int centerIdleTime) {
		this.centerIdleTime = centerIdleTime;
	}

	public void setAreaIdleTime(int areaIdleTime) {
		this.areaIdleTime = areaIdleTime;
	}

	public void setLocalIdleTime(int localIdleTime) {
		this.localIdleTime = localIdleTime;
	}

	@Override
	public void initialize() {
		this.monitor = new Timer(true);
		this.reclaimer = new Worker<>(SNetBlock::recycle).setDaemon(true);
		reclaimer.initialize();
		monitor.schedule(new MonitorTask(arenas), 1000, -250);
	}

	@Override
	public void release() {

	}

	@Override
	public SNetBlock allocate(int capacity) {
		return null;
	}

	protected SNetResource createResource(int capacity) {
		return resourceFactory.create(capacity);
	}

	protected void recycleBlock(SNetBlock block) {
		recycleBlock(block, true);
	}

	protected void recycleBlocks(List<SNetBlock> blocks) {
		recycleBlocks(blocks, true);
	}

	protected void recycleBlock(SNetBlock block, boolean async) {
		if (async)
			reclaimer.execute(block);
		else
			block.recycle();
	}

	protected void recycleBlocks(List<SNetBlock> blocks, boolean async) {
		if (blocks == null || blocks.isEmpty())
			return;
		if (async)
			reclaimer.execute(new RecycleRunner(blocks));
		else {
			for (SNetBlock e : blocks) e.recycle();
		}
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
				block.recycle();
		}
	}

	protected static class MonitorTask extends TimerTask {
		protected final ConcurrentLinkedQueue<SNetBlockArena> arenas;

		public MonitorTask(ConcurrentLinkedQueue<SNetBlockArena> arenas) {
			this.arenas = arenas;
		}

		@Override
		public void run() {
			try {
				run0();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		protected void run0() {
			for (SNetBlockArena arena : arenas)
				arena.releaseBlock();
		}
	}

}
