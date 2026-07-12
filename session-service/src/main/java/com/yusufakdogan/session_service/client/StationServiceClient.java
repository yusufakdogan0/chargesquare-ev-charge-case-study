package com.yusufakdogan.session_service.client;

import com.yusufakdogan.session_service.dto.StationConnectorResponse;
import com.yusufakdogan.session_service.exception.ConnectorOccupiedException;
import com.yusufakdogan.session_service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class StationServiceClient {

    @Value("${station.service.url}")
    private String stationServiceUrl;

    private final RestClient.Builder restClientBuilder;

    public StationConnectorResponse getConnectorById(Long connectorId, String bearerToken) {
        try {
            RestClient client = restClientBuilder.baseUrl(stationServiceUrl).build();
            return client.get()
                    .uri("/connectors/{id}", connectorId)
                    .header("Authorization", "Bearer " + bearerToken)
                    .retrieve()
                    .body(StationConnectorResponse.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException("Connector not found with id: " + connectorId);
            }
            throw e;
        }
    }

    public StationConnectorResponse occupyConnector(Long connectorId, String bearerToken) {
        try {
            RestClient client = restClientBuilder.baseUrl(stationServiceUrl).build();
            return client.patch()
                    .uri("/connectors/{id}/occupy", connectorId)
                    .header("Authorization", "Bearer " + bearerToken)
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
}
