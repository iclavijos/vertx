package com.mimacom.ivan.vertx.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Provincia {

    private String nombre;
    private List<Poblacion> poblaciones = new ArrayList<>();
    private int escanyos;

    public Provincia(String nombre, int escanyos) {
        this.nombre = nombre;
        this.poblaciones = poblaciones;
        this.escanyos = escanyos;
    }

    public void addPoblacion(Poblacion p) {
        this.poblaciones.add(p);
    }

    public int getNumPoblaciones() {
        return poblaciones.size();
    }

    public int getCensoTotal() {
        return poblaciones.parallelStream().mapToInt(p -> p.getCenso()).sum();
    }

    @JsonIgnore
    public Poblacion getPoblacion(String nombrePoblacion) {
        Optional<Poblacion> optResult = poblaciones.stream().filter(p -> p.getNombre().equals(nombrePoblacion)).findFirst();
        return optResult.orElseThrow(() -> new RuntimeException("Poblacion " + nombrePoblacion + " no encontrada"));
    }

    @JsonIgnore
    public String toJSONString() throws RuntimeException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
