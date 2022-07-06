/*
 * Copyright (c) 2022, Xianguang Zhou <xianguang.zhou@outlook.com>. All rights reserved.
 */
package pers.zxg.coroutine.example.uppercase.server;

import static pers.zxg.coroutine.Coroutine.launch;
import static pers.zxg.coroutine.vertx.core.VertxCoroutine.await;
import static pers.zxg.coroutine.vertx.core.VertxCoroutine.run;

import java.util.concurrent.ExecutionException;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import pers.zxg.coroutine.vertx.core.streams.BufferStreamReader;
import pers.zxg.coroutine.vertx.core.streams.BufferStreamWriter;

/**
 * @author <a href="mailto:xianguang.zhou@outlook.com">Xianguang Zhou</a>
 */
public class Main {

	public static void main(String[] args) throws Exception {
		Vertx vertx = Vertx.vertx();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			vertx.close();
		}));
		run(() -> {
			try {
				NetServer netServer = await(vertx.createNetServer().connectHandler(netSocket -> {
					launch(() -> {
						BufferStreamReader reader = new BufferStreamReader(netSocket);
						BufferStreamWriter writer = new BufferStreamWriter(netSocket);
						try {
							do {
								String line = reader.readLine().toString();
								writer.write(line.toUpperCase());
								writer.drain();
							} while (!reader.atEnd());
						} catch (ExecutionException ex) {
							ex.printStackTrace();
						} finally {
							writer.end();
						}
					});
				}).listen(0, "127.0.0.1"));
				System.out.println("Server port: " + netServer.actualPort());
			} catch (ExecutionException ex) {
				ex.printStackTrace();
			}
		}, vertx.getOrCreateContext()).get();
	}
}
