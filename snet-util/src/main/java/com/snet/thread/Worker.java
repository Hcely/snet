package com.snet.thread;

import com.snet.Destroyable;
import com.snet.Initializable;
import com.snet.Shutdownable;

public interface Worker extends Initializable, Destroyable, Shutdownable {
}
