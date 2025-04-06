package com.example.desafioTecnico.endpoints;

import com.example.desafioTecnico.CallbackStorage;
import com.example.desafioTecnico.Constantes;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/oauth")
public class OAuthCallbackRest {

    @Autowired
    private CallbackStorage callbackStorage;

    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestParam String code) {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("client_id", Constantes.CLIENT_ID);
        requestBody.add("client_secret", Constantes.CLIENT_SECRET);
        requestBody.add("redirect_uri", Constantes.URL_LOCAL_BASE + Constantes.ENDPOINT_REDIRECT);
        requestBody.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return obterToken(requestBody, headers, restTemplate);
    }

    @GetMapping("/refreshToken")
    public ResponseEntity<String> refreshAccessToken(String refreshToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", callbackStorage.getCallback().getRefreshToken());
        formData.add("client_id", Constantes.CLIENT_ID);
        formData.add("client_secret", Constantes.CLIENT_SECRET);
        formData.add("refresh_token", refreshToken);
        return obterToken(formData, headers, restTemplate);
    }

    private ResponseEntity<String> obterToken(MultiValueMap<String, String> requestBody, HttpHeaders headers, RestTemplate restTemplate) {
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(Constantes.URL_HUBSPOT_BASE + Constantes.ENDPOINT_TOKEN, HttpMethod.POST, request, String.class);
            try {
                callbackStorage.armazenarResposta(response);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.ok(response.getBody());
        } catch (RestClientException e) {
            System.out.println("Houve um erro ao obter o token");
        }
        return null;
    }
}