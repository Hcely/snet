package com.snet.core;

import com.snet.Shutdownable;

import java.net.SocketAddress;

public interface SNetAcceptor extends Shutdownable {
	void listen(String ip, int port);

	void listen(SocketAddress address);

	void accept();
}
