package com.snet.buffer.block.impl;

import com.snet.Initializable;
import com.snet.Releasable;
import com.snet.buffer.block.BlockArenaUtil;
import com.snet.buffer.block.SNetBlock;
import com.snet.buffer.block.SNetBlockAllocator;
import com.snet.buffer.resource.SNetBufferResourceFactory;
import com.snet.buffer.resource.SNetResource;
import com.snet.util.MathUtil;
import com.snet.util.RuntimeUtil;
import com.snet.util.thread.Worker;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;

public class BlockArenaManager implements SNetBlockAllocator, Initializable, Releasable {
	protected final SNetBufferResourceFactory resourceFactory;
	protected Timer monitor;
	protected LongAdder blockCounter;
	protected CenterArena centerArena;
	protected AreaArena[] areaArenas;
	protected ConcurrentLinkedQueue<LocalBlockArena> localArenas;
	protected ThreadLocal<LocalBlockArena> tlArenas;

	protected int blockCapacity;
	protected int areaNum;
	protected int centerIdleTime;
	protected int areaIdleTime;
	protected int localIdleTime;
	protected boolean released;

	public BlockArenaManager(SNetBufferResourceFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
		this.areaNum = MathUtil.ceil2(RuntimeUtil.DOUBLE_CORE_PROCESSOR);
		this.blockCapacity = 1 << 21;
		this.centerIdleTime = 10000;
		this.areaIdleTime = 5000;
		this.localIdleTime = 2500;
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
		this.centerArena = new CenterArena(this, blockCapacity);
		this.areaArenas = new AreaArena[areaNum];
		this.localArenas = new ConcurrentLinkedQueue<>();
		this.tlArenas = ThreadLocal.withInitial(() -> {
			Thread thread = Thread.currentThread();
			LocalBlockArena arena = new LocalBlockArena(BlockArenaManager.this, areaArenas[(int) (thread.getId() & (areaNum - 1))]);
			localArenas.add(arena);
			return arena;
		});
		this.blockCounter = new LongAdder();
		this.monitor = new Timer(true);
		this.reclaimer = new Worker<>(SNetBlock::recycle).setDaemon(true);

		reclaimer.initialize();
		monitor.schedule(new MonitorTask(centerArena, areaArenas, localArenas), 1000, -250);
	}

	@Override
	public void release() {
		released = true;
	}

	@Override
	public SNetBlock allocate(int capacity) {
		if (released)
			return null;
		final LocalBlockArena arena = tlArenas.get();
		return arena.allocate(BlockArenaUtil.normalCapacity(capacity));
	}

	protected SNetResource createResource(int capacity) {
		return resourceFactory.create(capacity);
	}

	protected void incBlockCount() {
		blockCounter.add(1L);
	}

	protected void decBlockCount() {
		blockCounter.add(-1L);
		if (released && blockCounter.sum() == 0L) {
			centerArena.trimArena();
			monitor.cancel();
			reclaimer.destroy();
		}
	}

	protected void recycleBlock(SNetBlock block) {
		reclaimer.execute(block);
	}

	protected void recycleBlocks(List<SNetBlock> blocks) {
		if (blocks == null || blocks.isEmpty())
			return;
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

	protected static class MonitorTask extends TimerTask {
		protected CenterArena centerArena;
		protected AreaArena[] areaArenas;
		protected ConcurrentLinkedQueue<LocalBlockArena> localArenas;

		public MonitorTask(CenterArena centerArena, AreaArena[] areaArenas, ConcurrentLinkedQueue<LocalBlockArena> localArenas) {
			this.centerArena = centerArena;
			this.areaArenas = areaArenas;
			this.localArenas = localArenas;
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
			for (Iterator<LocalBlockArena> it = localArenas.iterator(); it.hasNext(); ) {
				LocalBlockArena arena = it.next();
				arena.trimArena();
				if (!arena.isAlive()) {
					arena.trimArena();
					it.remove();
				}
			}
			for (AreaArena arena : areaArenas)
				arena.trimArena();
			centerArena.trimArena();
		}
	}

}
