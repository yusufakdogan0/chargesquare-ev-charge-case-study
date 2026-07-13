package com.yusufakdogan.session_service.client;

import com.yusufakdogan.session_service.dto.StationConnectorResponse;
import com.yusufakdogan.session_service.exception.ConnectorOccupiedException;
import com.yusufakdogan.session_service.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class StationServiceClient {

    @Value("${station.service.url}")
    private String stationServiceUrl;

    private final RestClient.Builder restClientBuilder;

    private RestClient getClient() {
        return restClientBuilder
                .baseUrl(stationServiceUrl)
                .requestInterceptor((request, body, execution) -> {
                    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attrs != null) {
                        HttpServletRequest servletRequest = attrs.getRequest();
                        String authHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
                        if (authHeader != null) {
                            request.getHeaders().add(HttpHeaders.AUTHORIZATION, authHeader);
                        }
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

    public StationConnectorResponse getConnectorById(Long connectorId) {
        try {
            return getClient().get()
                    .uri("/connectors/{id}", connectorId)
                    .retrieve()
                    .body(StationConnectorResponse.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException("Connector not found with id: " + connectorId);
            }
            throw e;
        }
    }

    public StationConnectorResponse occupyConnector(Long connectorId) {
        try {
            return getClient().patch()
                    .uri("/connectors/{id}/occupy", connectorId)
                    .retrieve()
                    .body(StationConnectorResponse.class);
        } catch (HttpClientErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            if (statusCode == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException("Connector not found with id: " + connectorId);
            } else if (statusCode == HttpStatus.CONFLICT) {
                throw new ConnectorOccupiedException("Connector " + connectorId + " is already occupied");
            }
            throw e;
        }
    }

    public StationConnectorResponse releaseConnector(Long connectorId) {
        try {
            return getClient().patch()
                    .uri("/connectors/{id}/release", connectorId)
                    .retrieve()
                    .body(StationConnectorResponse.class);
        } catch (HttpClientErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            if (statusCode == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException("Connector not found with id: " + connectorId);
            } else if (statusCode == HttpStatus.CONFLICT) {
                throw new ConnectorOccupiedException("Connector " + connectorId + " is already available");
            }
            throw e;
        }
    }
}
