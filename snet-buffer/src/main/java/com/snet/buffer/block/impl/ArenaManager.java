package com.snet.buffer.block.impl;

import com.snet.Initializable;
import com.snet.Releasable;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.resource.SNetBufferResourceFactory;
import com.snet.buffer.resource.SNetResource;
import com.snet.util.thread.Worker;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ArenaManager implements Initializable, Releasable {

	protected Timer monitor;
	protected Worker<SNetBlock> reclaimer;
	protected final ConcurrentLinkedQueue<SNetBlockArena> arenas;
	protected final SNetBufferResourceFactory resourceFactory;
	protected int centerIdleTime;
	protected int areaIdleTime;
	protected int localIdleTime;

	public ArenaManager(SNetBufferResourceFactory resourceFactory) {
		this.arenas = new ConcurrentLinkedQueue<>();
		this.resourceFactory = resourceFactory;
		this.centerIdleTime = 15000;
		this.areaIdleTime = 10000;
		this.localIdleTime = 5000;
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
		this.reclaimer = new Worker<SNetBlock>(e -> e.getArena().recycle(e)).setDaemon(true);
		reclaimer.initialize();
		monitor.schedule(new MonitorTask(arenas), 1000, 1000);
	}

	@Override
	public void release() {

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

		}
	}

}
