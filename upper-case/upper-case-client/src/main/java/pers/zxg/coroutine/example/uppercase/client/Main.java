/*
 * Copyright (c) 2022, Xianguang Zhou <xianguang.zhou@outlook.com>. All rights reserved.
 */
package pers.zxg.coroutine.example.uppercase.client;

import static pers.zxg.coroutine.vertx.core.VertxCoroutine.await;
import static pers.zxg.coroutine.vertx.core.VertxCoroutine.callBlocking;
import static pers.zxg.coroutine.vertx.core.VertxCoroutine.run;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.net.NetSocket;
import pers.zxg.coroutine.vertx.core.streams.BufferStreamReader;
import pers.zxg.coroutine.vertx.core.streams.BufferStreamWriter;

/**
 * @author <a href="mailto:xianguang.zhou@outlook.com">Xianguang Zhou</a>
 */
public class Main {

	public static void main(String[] args) throws Exception {
		int port = Integer.parseInt(args[0]);
		Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(1));
		try {
			run(() -> {
				try {
					NetSocket netSocket = await(vertx.createNetClient().connect(port, "127.0.0.1"));
					try {
						BufferStreamReader reader = new BufferStreamReader(netSocket);
						BufferStreamWriter writer = new BufferStreamWriter(netSocket);
						BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
						PrintStream stdout = System.out;
						while (true) {
							stdout.print("Input : ");
							String input = callBlocking(() -> stdin.readLine());
							if (input == null) {
								stdout.println();
								break;
							}
							writer.write(input + '\n');
							writer.drain();
							String output = reader.readLine().toString();
							stdout.print("Output: " + output);
						}
					} finally {
						netSocket.end();
					}
				} catch (ExecutionException ex) {
					ex.printStackTrace();
				}
			}, vertx.getOrCreateContext()).get();
		} finally {
			vertx.close();
		}
	}
}
