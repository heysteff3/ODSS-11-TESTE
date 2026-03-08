package com.sustentafome.sustentafome.donation;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationGateway {
    private final RestTemplate restTemplate;

    @Value("${app.notifications.url:}")
    private String notificationUrl;

    @Retry(name = "notification")
    @CircuitBreaker(name = "notification", fallbackMethod = "onFailure")
    public void enviarAlerta(Alerta alerta) {
        if (notificationUrl == null || notificationUrl.isBlank()) {
            return;
        }
        restTemplate.postForLocation(notificationUrl, alerta);
    }

    @SuppressWarnings("unused")
    private void onFailure(Alerta alerta, Throwable throwable) {
        log.warn("Falha ao enviar notificacao externa para alerta {}: {}", alerta != null ? alerta.getId() : "novo", throwable.getMessage());
    }
}
