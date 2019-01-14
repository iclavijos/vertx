package com.mimacom.ivan.vertx.server.verticles;

import com.mimacom.ivan.vertx.server.model.Pais;
import com.mimacom.ivan.vertx.server.model.Poblacion;
import com.mimacom.ivan.vertx.server.model.Provincia;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.ThreadLocalRandom;

public class DemografiaVerticle extends AbstractVerticle {

    private static final int MIN_PROVS = 1, MAX_PROVS = 5;
    private static final int MIN_POBS = 1, MAX_POBS = 5;
    private static final int MIN_COLS = 1, MAX_COLS = 5;

    @Override
    public void start(Future<Void> fut) {

        // Create a router object.
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/api/demographics").handler(this::getDemographics);
        router.post("/api/recuento").handler(this::procesaRecuentoColegio);

        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        // Retrieve the port from the configuration,
                        // default to 8080.
                        config().getInteger("http.port", 8080),
                        result -> {
                            if (result.succeeded()) {
                                fut.complete();
                            } else {
                                fut.fail(result.cause());
                            }
                        }
                );
    }

    private void getDemographics(RoutingContext rc) {
        int numProvincias = ThreadLocalRandom.current().nextInt(MIN_PROVS, MAX_PROVS + 1);
        Pais p = new Pais();
        SharedData sd = vertx.sharedData();
        LocalMap<String, Buffer> mapProvincias = sd.getLocalMap("mapProvincias");
        mapProvincias.clear();
        LocalMap<String, Buffer> votosPoblacion = sd.getLocalMap("mapVotosPoblacion");
        votosPoblacion.clear();
        LocalMap<String, Buffer> votosProvincia = sd.getLocalMap("mapVotosProvincia");
        votosProvincia.clear();
        LocalMap<String, Buffer> votosPais = sd.getLocalMap("mapVotosPais");
        votosPais.clear();

        for(int i = 0; i < numProvincias; i++) {
            int numPoblaciones = ThreadLocalRandom.current().nextInt(MIN_POBS, MAX_POBS + 1);
            Provincia prov = new Provincia(
                    "Provincia " + RandomStringUtils.random(4, true, false),
                    ThreadLocalRandom.current().nextInt(3, 12 + 1));
            p.addProvincia(prov);

            for(int j = 0; j < numPoblaciones; j++) {
                int numColegios = ThreadLocalRandom.current().nextInt(MIN_COLS, MAX_COLS + 1);
                double factor = new Double(numColegios) / new Double(50);
                int censo = ThreadLocalRandom.current().nextInt(100, 1500000 + 1);
                censo = new Double((new Double(censo) * factor)).intValue();
                censo = (censo / numColegios) * numColegios;
                Poblacion pob = new Poblacion(
                        RandomStringUtils.random(ThreadLocalRandom.current().nextInt(3, 10 + 1), true, false),
                        numColegios,
                        censo);
                prov.addPoblacion(pob);
            }
            mapProvincias.put(prov.getNombre(), Buffer.buffer(prov.toJSONString()));
        }
        System.out.println(p);
        rc.response()
                .putHeader("content-type",
                        "application/json; charset=utf-8")
                .end(Json.encodePrettily(p));
    }

    private void procesaRecuentoColegio(RoutingContext rc) {
        vertx.eventBus().publish("recuentoColegio", rc.getBodyAsJson());

        rc.response().end();
    }
}
