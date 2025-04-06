package com.example.desafioTecnico.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebhookEvent {
    private Long eventId;
    private Long subscriptionId;
    private Long portalId;
    private Long appId;
    private Long occurredAt;
    private String subscriptionType;
    private Long objectId;
}
