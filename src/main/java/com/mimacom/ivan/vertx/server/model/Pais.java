package com.mimacom.ivan.vertx.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Pais {
    private List<Provincia> provincias = new ArrayList<>();

    public void addProvincia(Provincia p) {
        this.provincias.add(p);
    }

    public int getNumProvincias() {
        return provincias.size();
    }

    @JsonIgnore
    public Provincia getProvincia(String nombreProvincia) {
        return provincias.parallelStream().filter(p -> p.getNombre().equals(nombreProvincia)).findFirst()
                .orElseThrow(() -> new RuntimeException("Provincia " + nombreProvincia + " no encontrada"));
    }
}
