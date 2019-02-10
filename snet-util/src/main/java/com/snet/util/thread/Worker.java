package com.snet.util.thread;

import com.snet.Initializable;
import com.snet.Shutdownable;

public interface Worker<T> extends Initializable, Shutdownable, WorkExecutor<T> {
	
}
