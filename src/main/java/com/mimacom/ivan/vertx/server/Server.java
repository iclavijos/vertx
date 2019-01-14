package com.mimacom.ivan.vertx.server;

import com.mimacom.ivan.vertx.server.verticles.MainVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Server {

    public static void main(String[] args) throws Exception {
        BlockingQueue<AsyncResult<String>> q = new ArrayBlockingQueue<>(1);
        Vertx.vertx().deployVerticle(new MainVerticle(), q::offer);
        AsyncResult<String> result = q.take();
        if (result.failed()) {
            throw new RuntimeException(result.cause());
        }
    }
}
