package com.mimacom.ivan.vertx.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Poblacion {
    private String nombre;
    private int colegiosElectorales;
    private int censo;

    public Poblacion(String nombre, int colegiosElectorales, int censo) {
        this.nombre = nombre;
        this.colegiosElectorales = colegiosElectorales;
        this.censo = censo;
    }

}
