package com.mimacom.ivan.vertx.server.verticles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimacom.ivan.vertx.server.model.Provincia;
import com.mimacom.ivan.vertx.server.model.RecuentoVotos;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

import java.io.IOException;

public class RecuentoProvinciaVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {
        EventBus eb = vertx.eventBus();
        final SharedData sd = vertx.sharedData();

        eb.consumer("recuentoPoblacion", message -> {
            WorkerExecutor executor = vertx.createSharedWorkerExecutor("poblacion-worker-pool");
            executor.executeBlocking(future -> {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String data = (String)message.body();
                    RecuentoVotos votosPoblacion = mapper.readValue(data, RecuentoVotos.class);

                    LocalMap<String, Buffer> votosProvincia = sd.getLocalMap("mapVotosProvincia");
                    LocalMap<String, Buffer> mapProvincias = sd.getLocalMap("mapProvincias");

                    Buffer buff = mapProvincias.get(votosPoblacion.getProvincia());
                    Provincia provincia = buff.toJsonObject().mapTo(Provincia.class);

                    RecuentoVotos votosProv = null;
                    if (votosProvincia.containsKey(provincia.getNombre())) {
                        votosProv = votosProvincia.get(provincia.getNombre()).toJsonObject().mapTo(RecuentoVotos.class);
                    } else {
                        votosProv = new RecuentoVotos();
                        votosProv.setProvincia(provincia.getNombre());
                    }
                    votosProv.procesaVotos(votosPoblacion.getVotos());

                    votosProvincia.put(provincia.getNombre(), Buffer.buffer(votosProv.toJSONString()));

                    if (votosProv.getItemsContabilizados() == provincia.getNumPoblaciones()) {
                        vertx.eventBus().publish("recuentoProvincia", votosProv.toJSONString());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                future.complete();
            }, res -> {

            });
        });
    }
}
