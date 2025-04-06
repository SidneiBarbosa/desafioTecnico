package com.example.desafioTecnico.endpoints;

import com.example.desafioTecnico.entities.WebhookEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/webhook")
public class WebhookRest {

    @PostMapping("/hubspot")
    public ResponseEntity<Void> receberWebhook(@RequestBody List<WebhookEvent> eventos) {
        try {
            eventos.forEach(evento -> {
                System.out.println("🚀 Webhook recebido: " + evento);
                if ("contact.creation".equals(evento.getSubscriptionType())) {
                    System.out.println("🟢 Novo contato criado com ID: " + evento.getObjectId());
                } else {
                    System.out.println("🟢 Erro ao criar contato com ID: " + evento.getObjectId());
                }
            });

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }
}