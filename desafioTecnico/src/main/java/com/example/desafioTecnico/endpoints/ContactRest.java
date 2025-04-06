package com.example.desafioTecnico.endpoints;

import com.example.desafioTecnico.CallbackStorage;
import com.example.desafioTecnico.Constantes;
import com.example.desafioTecnico.Utils;
import com.example.desafioTecnico.entities.Contact;
import com.example.desafioTecnico.entities.WebhookEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hubspot")
public class ContactRest {

    @Autowired
    private CallbackStorage callbackStorage;

    @PostMapping("/contato")
    public ResponseEntity<String> criarContato(@RequestBody Contact contactRequest) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (isTokenValid()) {
            headers.setBearerAuth(callbackStorage.getCallback().getAccessToken());
        } else {
            if (callbackStorage.getCallback() != null) {
                String refreshToken = callbackStorage.getCallback().getRefreshToken();

                String url = "http://localhost:8080/refreshToken?refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    try {
                        callbackStorage.armazenarResposta(response);
                        headers.setBearerAuth(callbackStorage.getCallback().getAccessToken());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    System.out.println("Erro ao criar contato");
                }
            }
        }
        Map<String, Object> body = new HashMap<>();
        Map<String, String> contato = montarContato(contactRequest);
        body.put("properties", contato);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    Constantes.URL_HUBSPOT_BASE + Constantes.ENDPOINT_CONTACTS_HUBSPOT,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                Long idContato = extrairIdContatoDaResposta(response.getBody());

                WebhookEvent eventoSimulado = new WebhookEvent();
                eventoSimulado.setSubscriptionType("contact.creation");
                eventoSimulado.setObjectId(idContato);

                List<WebhookEvent> eventos = List.of(eventoSimulado);

                HttpHeaders headersWebhook = new HttpHeaders();
                headersWebhook.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<List<WebhookEvent>> requestWebhook = new HttpEntity<>(eventos, headersWebhook);

                restTemplate.postForEntity( Constantes.URL_LOCAL_BASE + Constantes.ENDPOINT_WEBHOOK, requestWebhook, Void.class);
            }
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar contato: " + e.getMessage());
        }
    }

    private boolean isTokenValid() {
        return callbackStorage.getCallback() != null
                && Utils.adicionarSegundos(callbackStorage.getCallback().getDataCriacao(),
                callbackStorage.getCallback().getExpiresIn())
                .after(new Date());
    }

    private static Map<String, String> montarContato(Contact contactRequest) {
        Map<String, String> properties = new HashMap<>();
        properties.put("email", contactRequest.getEmail());
        properties.put("firstname", contactRequest.getFirstname());
        properties.put("lastname", contactRequest.getLastname());
        properties.put("phone", contactRequest.getPhone());
        properties.put("company", contactRequest.getCompany());
        return properties;
    }

    private Long extrairIdContatoDaResposta(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            return root.has("id") ? root.get("id").asLong() : null;
        } catch (Exception e) {
            System.out.println("Não foi possível obter a resposta do contato");
            return null;
        }
    }
}
