package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.ListEndpointsRequest;
import software.amazon.awssdk.services.sagemaker.model.ListEndpointsResponse;
import software.amazon.awssdk.services.sagemaker.model.EndpointSummary;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointRequest;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class Handler {
    private static final Logger logger = LoggerFactory.getLogger(Handler.class);
    private final SageMakerRuntimeClient sageMakerRuntime;
    private final SageMakerClient sageMakerClient;

    public Handler() {
        this.sageMakerRuntime = DependencyFactory.sageMakerRuntimeClient();
        this.sageMakerClient = DependencyFactory.sageMakerClient();
    }

    public List<EndpointSummary> listEndpoints() {
        try {
            ListEndpointsRequest request = ListEndpointsRequest.builder().build();
            ListEndpointsResponse result = sageMakerClient.listEndpoints(request);
            List<EndpointSummary> endpoints = result.endpoints();
            
            logger.info("Found {} endpoints", endpoints.size());
            for (EndpointSummary endpoint : endpoints) {
                logger.info("Endpoint: {}", endpoint.endpointName());
            }
            
            return endpoints;
        } catch (Exception e) {
            logger.error("Error listing SageMaker endpoints", e);
            throw new RuntimeException("Failed to list SageMaker endpoints", e);
        }
    }

    public String invokeEndpoint(String endpointName, String inputText) {
        try {
            SdkBytes inputBytes = SdkBytes.fromString(inputText, StandardCharsets.UTF_8);

            InvokeEndpointRequest request = InvokeEndpointRequest.builder()
                    .endpointName(endpointName)
                    .contentType("text/plain")
                    .body(inputBytes)
                    .build();

            InvokeEndpointResponse result = sageMakerRuntime.invokeEndpoint(request);

            String responseBody = result.body().asUtf8String();
            logger.info("SageMaker endpoint response: {}", responseBody);

            return responseBody;

        } catch (Exception e) {
            logger.error("Error invoking SageMaker endpoint", e);
            throw new RuntimeException("Failed to invoke SageMaker endpoint", e);
        }
    }

    public void sendRequest(String endpointName, String inputText) {
        if (endpointName == null || endpointName.isEmpty()) {
            List<EndpointSummary> endpoints = listEndpoints();
            if (!endpoints.isEmpty()) {
                endpointName = endpoints.get(0).endpointName();
                logger.info("Using first available endpoint: {}", endpointName);
            } else {
                logger.error("No endpoints available");
                return;
            }
        }

        if (inputText == null || inputText.isEmpty()) {
            inputText = "Default query text";
            logger.info("Using default input text: {}", inputText);
        }

        try {
            String response = invokeEndpoint(endpointName, inputText);
            logger.info("Received response from endpoint {}: {}", endpointName, response);
        } catch (Exception e) {
            logger.error("Error sending request to endpoint {}", endpointName, e);
        }
    }
}
