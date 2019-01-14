package com.mimacom.ivan.vertx.server.verticles;

import com.mimacom.ivan.vertx.server.model.Poblacion;
import com.mimacom.ivan.vertx.server.model.Provincia;
import com.mimacom.ivan.vertx.server.model.VotosColegio;
import com.mimacom.ivan.vertx.server.model.RecuentoVotos;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

public class RecuentoPoblacionVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {
        EventBus eb = vertx.eventBus();
        final SharedData sd = vertx.sharedData();

        eb.consumer("recuentoColegio", message -> {
            WorkerExecutor executor = vertx.createSharedWorkerExecutor("colegio-worker-pool");
            executor.executeBlocking(future -> {
                VotosColegio votosColegio = ((JsonObject)message.body()).mapTo(VotosColegio.class);
                LocalMap<String, Buffer> mapProvincias = sd.getLocalMap("mapProvincias");
                Buffer buff = mapProvincias.get(votosColegio.getProvincia());
                Provincia provincia = buff.toJsonObject().mapTo(Provincia.class);
                Poblacion poblacion = provincia.getPoblacion(votosColegio.getPoblacion());

                LocalMap<String, Buffer> votosPoblacion = sd.getLocalMap("mapVotosPoblacion");
                RecuentoVotos votosPob = null;
                if (votosPoblacion.containsKey(poblacion.getNombre())) {
                    votosPob = votosPoblacion.get(poblacion.getNombre()).toJsonObject().mapTo(RecuentoVotos.class);
                } else {
                    votosPob = new RecuentoVotos();
                    votosPob.setPoblacion(poblacion.getNombre());
                    votosPob.setProvincia(provincia.getNombre());
                }
                votosPob.procesaVotos(votosColegio.getVotos());

                votosPoblacion.put(poblacion.getNombre(), Buffer.buffer(votosPob.toJSONString()));

                if (votosPob.getItemsContabilizados() == poblacion.getColegiosElectorales()) {
                    vertx.eventBus().publish("recuentoPoblacion", votosPob.toJSONString());
                    //System.out.println("Procesada poblacion " + poblacion.getNombre());
                }

                future.complete(provincia.toJSONString());
            }, res -> {
                if (res.failed()) {
                    res.cause().printStackTrace(System.err);
                }
                //System.out.println("The result is: " + res.result());
            });
        });
    }
}
