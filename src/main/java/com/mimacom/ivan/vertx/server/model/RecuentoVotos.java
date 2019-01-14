package com.mimacom.ivan.vertx.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
public class RecuentoVotos {
    private String poblacion;
    private String provincia;
    private List<VotosPartido> votos = new ArrayList<>();
    private int votosContabilizados = 0;
    private int itemsContabilizados = 0;

    public void incColegiosContabilizados() {
        itemsContabilizados += 1;
    }

    public void procesaVotos(List<VotosPartido> votosPartidos) {
        this.itemsContabilizados++;
        List<VotosPartido> acum = new ArrayList<>();

        votosPartidos.stream().forEach(votPartido -> {
            VotosPartido votosPartido = null;
            Optional<VotosPartido> optVotosPartido = votos.stream()
                    .filter(
                            v -> v.getPartido().equals(votPartido.getPartido()))
                    .findFirst();
            if (optVotosPartido.isPresent()) {
                votosPartido = optVotosPartido.get();
                votosPartido.setVotos(votosPartido.getVotos() + votPartido.getVotos());
            } else {
                votosPartido = new VotosPartido();
                votosPartido.setPartido(votPartido.getPartido());
                votosPartido.setVotos(votPartido.getVotos());
            }
            votosContabilizados += votPartido.getVotos();

            acum.add(votosPartido);
        });
        this.votos = acum;
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
