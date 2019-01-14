package com.mimacom.ivan.vertx.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class VotosColegio {

    private String provincia;
    private String poblacion;
    private int numColegio;
    private int participacion;
    private List<VotosPartido> votos;

    public int getTotalVotosColegio() {
        return votos.parallelStream().map(votosPartido -> votosPartido.getVotos()).reduce(0, Integer::sum);
    }
}
