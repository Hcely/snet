package com.snet.buffer.block.impl;

import com.snet.Initializable;
import com.snet.Releasable;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockArena;
import com.snet.buffer.resource.SNetBufferResourceFactory;
import com.snet.buffer.resource.SNetResource;
import com.snet.util.MathUtil;
import com.snet.util.RuntimeUtil;
import com.snet.util.thread.Worker;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ArenaManager implements Initializable, Releasable {
	protected final ConcurrentLinkedDeque<SNetBlockArena> arenas;
	protected final SNetBufferResourceFactory resourceFactory;
	protected final int blockCapacity;
	protected Timer monitor;
	protected Worker<SNetBlock> reclaimer;
	protected CenterBlockArena centerArena;
	protected AreaBlockArena[] areaArenas;
	protected int centerIdleTime;
	protected int areaIdleTime;
	protected int localIdleTime;
	protected boolean released;

	public ArenaManager(int blockCapacity, SNetBufferResourceFactory resourceFactory) {
		this.arenas = new ConcurrentLinkedDeque<>();
		this.resourceFactory = resourceFactory;
		this.centerIdleTime = 15000;
		this.areaIdleTime = 10000;
		this.localIdleTime = 5000;
		this.blockCapacity = blockCapacity;
		this.released = false;
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
		this.centerArena = new CenterBlockArena(this, blockCapacity);
		this.areaArenas = new AreaBlockArena[MathUtil.ceil2(RuntimeUtil.DOUBLE_CORE_PROCESSOR)];
		for (int i = 0, len = areaArenas.length; i < len; ++i)
			areaArenas[i] = new AreaBlockArena(this, centerArena);
		reclaimer.initialize();
		monitor.schedule(new MonitorTask(arenas), 1000, 1000);
		arenas.addFirst(centerArena);
		for (AreaBlockArena e : areaArenas)
			arenas.addFirst(e);
	}

	@Override
	public void release() {
		if (released)
			return;
		released = true;
		centerArena.release();
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
				block.recycle();
		}
	}

	protected static class MonitorTask extends TimerTask {
		protected final ConcurrentLinkedDeque<SNetBlockArena> arenas;

		public MonitorTask(ConcurrentLinkedDeque<SNetBlockArena> arenas) {
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
			for (Iterator<SNetBlockArena> it = arenas.iterator(); it.hasNext(); ) {
				SNetBlockArena arena = it.next();
				arena.releaseBlock();
				if (arena.isReleased())
					it.remove();
			}
		}
	}

}
