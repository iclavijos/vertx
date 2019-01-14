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

public class RecuentoPaisVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {
        EventBus eb = vertx.eventBus();
        final SharedData sd = vertx.sharedData();

        eb.consumer("recuentoProvincia", message -> {
            WorkerExecutor executor = vertx.createSharedWorkerExecutor("provincia-worker-pool");
            executor.executeBlocking(future -> {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String data = (String)message.body();
                    RecuentoVotos votosProvincia = mapper.readValue(data, RecuentoVotos.class);

                    LocalMap<String, Buffer> mapProvincias = sd.getLocalMap("mapProvincias");
                    LocalMap<String, Buffer> votosPais = sd.getLocalMap("mapVotosPais");
                    
                    RecuentoVotos votosProv = null;
                    if (votosPais.containsKey(votosProvincia.getProvincia())) {
                        votosProv = votosPais.get(votosProvincia.getProvincia()).toJsonObject().mapTo(RecuentoVotos.class);
                    } else {
                        votosProv = new RecuentoVotos();
                        votosProv.setProvincia(votosProvincia.getProvincia());
                    }
                    votosProv.procesaVotos(votosProvincia.getVotos());

                    votosPais.put(votosProvincia.getProvincia(), Buffer.buffer(votosProv.toJSONString()));

                    Buffer buff = mapProvincias.get(votosProv.getProvincia());
                    Provincia provincia = buff.toJsonObject().mapTo(Provincia.class);

                    printResultadoProvincia(provincia, votosProv);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                future.complete();
            }, res -> {

            });
        });
    }

    public void printResultadoProvincia(Provincia provincia, RecuentoVotos votosProvincia) {
        String res = "%s: Participacion: %f%%";
        final double votos = votosProvincia.getVotosContabilizados();
        final double censo = provincia.getCensoTotal();
        final int votosEscanyo = votosProvincia.getVotosContabilizados() / provincia.getEscanyos();

        String partidoStr = "\t%s: %d escanyos (%f%% votos)";
        System.out.println(String.format(res, provincia.getNombre(), (votos / censo) * 100d));
        votosProvincia.getVotos().forEach(partido -> {
            double votosPartido = partido.getVotos();
            System.out.println(String.format(partidoStr, partido.getPartido(), 32, (votosPartido / votos) * 100d));
        });
    }
}
