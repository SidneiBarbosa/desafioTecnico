package com.example.desafioTecnico.endpoints;

import com.example.desafioTecnico.Constantes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class OAuthRest {

    @GetMapping("/authorize")
    public ResponseEntity<String> getAuthorizationUrl() {
        try {
            String url = Constantes.AUTHORIZATION_URL +
                    "?client_id=" + Constantes.CLIENT_ID +
                    "&redirect_uri=" + Constantes.URL_LOCAL_BASE + Constantes.ENDPOINT_REDIRECT +
                    "&scope=crm.objects.contacts.write%20crm.objects.contacts.read" +
                    "&response_type=code";
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            System.out.println("Erro ao obter a URL de autenticação.");
        }
        return null;
    }
}