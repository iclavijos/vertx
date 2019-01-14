package com.mimacom.ivan.vertx.server.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {

        CompositeFuture.all(deployHelper(DemografiaVerticle.class.getName()),
                deployHelper(RecuentoPoblacionVerticle.class.getName()),
                deployHelper(RecuentoProvinciaVerticle.class.getName()),
                deployHelper(RecuentoPaisVerticle.class.getName())).setHandler(result -> {
            if (result.succeeded()) {
                fut.complete();
            } else {
                fut.fail(result.cause());
            }
        });
    }

    private Future<Void> deployHelper(String name) {
        final Future<Void> future = Future.future();
        vertx.deployVerticle(name, res -> {
            if(res.failed()){
                log.error("Failed to deploy verticle " + name);
                future.fail(res.cause());
            } else {
                log.info("Deployed verticle " + name);
                future.complete();
            }
        });
        vertx.exceptionHandler(new Handler<Throwable>() {
            @Override
            public void handle(Throwable event) {
                // do what you meant to do on uncaught exception, e.g.:
                event.printStackTrace(System.err);
                //someLogger.error(event + " throws exception: " + event.getStackTrace());
            }
        });

        return future;
    }
}
