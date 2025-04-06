package com.example.desafioTecnico;

import com.example.desafioTecnico.entities.Callback;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Getter
@Setter
public class CallbackStorage {

    private Callback callback;

    public boolean hasCallback() {
        return this.callback != null;
    }

    public void armazenarResposta(ResponseEntity<String> response) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Callback callback = mapper.readValue(response.getBody(), Callback.class);
        callback.setDataCriacao(new Date());
        setCallback(callback);
    }
}
