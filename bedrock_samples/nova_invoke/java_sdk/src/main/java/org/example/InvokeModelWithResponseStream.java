// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package org.example;

// Use the native inference API to send a text message to Amazon Nova
// and print the response stream.

import org.json.JSONArray;
import org.json.JSONObject;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamResponseHandler;

import java.util.concurrent.ExecutionException;

import static software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamResponseHandler.Visitor;

public class InvokeModelWithResponseStream {

    public static String invokeModelWithResponseStream() {
        // Create a Bedrock Runtime client in the AWS Region you want to use.
        BedrockRuntimeAsyncClient client = null;
        
        try {
            client = BedrockRuntimeAsyncClient.builder()
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .region(Region.US_EAST_1)
                    .build();

            // Set the model ID for Nova
            String modelId = "us.amazon.nova-lite-v1:0";

            // The InvokeModelWithResponseStream API uses the model's native payload.
            // Nova uses the messages format with a specific schema version
            var nativeRequest = """
                    {
                        "schemaVersion": "messages-v1",
                        "inferenceConfig": {
                            "maxTokens": 500,
                            "temperature": 0.7,
                            "topP": 0.9,
                            "topK": 20
                        },
                        "messages": [
                            {
                                "role": "user",
                                "content": [
                                    {
                                        "text": "Describe the purpose of a 'hello world' program in one line."
                                    }
                                ]
                            }
                        ]
                    }""";

            // Create a request with the model ID and the model's native request payload.
            var request = InvokeModelWithResponseStreamRequest.builder()
                    .body(SdkBytes.fromUtf8String(nativeRequest))
                    .modelId(modelId)
                    .build();

            // Prepare a buffer to accumulate the generated response text.
            var completeResponseTextBuffer = new StringBuilder();

            // Prepare a handler to extract, accumulate, and print the response text in real-time.
            var responseStreamHandler = InvokeModelWithResponseStreamResponseHandler.builder()
                    .subscriber(Visitor.builder().onChunk(chunk -> {
                        try {
                            // Get the raw response for parsing
                            String rawResponse = chunk.bytes().asUtf8String();
                            // System.out.println("Raw response chunk: " + rawResponse);
                            
                            var response = new JSONObject(rawResponse);
                            
                            // Parse contentBlockDelta format
                            if (response.has("contentBlockDelta")) {
                                JSONObject contentBlockDelta = response.getJSONObject("contentBlockDelta");
                                if (contentBlockDelta.has("delta") && contentBlockDelta.getJSONObject("delta").has("text")) {
                                    String text = contentBlockDelta.getJSONObject("delta").getString("text");
                                    System.out.print(text);  // Print in real-time
                                    completeResponseTextBuffer.append(text);
                                }
                            }
                            // Parse metadata information
                            else if (response.has("metadata")) {
                                System.out.println("\n\n=== METADATA INFORMATION ===");
                                
                                // Parse usage information
                                if (response.getJSONObject("metadata").has("usage")) {
                                    JSONObject usage = response.getJSONObject("metadata").getJSONObject("usage");
                                    System.out.println("Usage Information:");
                                    System.out.println("- Input Tokens: " + usage.getInt("inputTokens"));
                                    System.out.println("- Output Tokens: " + usage.getInt("outputTokens"));
                                    System.out.println("- Cache Read Input Tokens: " + usage.getInt("cacheReadInputTokenCount"));
                                    System.out.println("- Cache Write Input Tokens: " + usage.getInt("cacheWriteInputTokenCount"));
                                }
                                
                                // Parse invocation metrics if available
                                if (response.has("amazon-bedrock-invocationMetrics")) {
                                    JSONObject metrics = response.getJSONObject("amazon-bedrock-invocationMetrics");
                                    System.out.println("\nInvocation Metrics:");
                                    System.out.println("- Input Token Count: " + metrics.getInt("inputTokenCount"));
                                    System.out.println("- Output Token Count: " + metrics.getInt("outputTokenCount"));
                                    System.out.println("- Invocation Latency: " + metrics.getInt("invocationLatency") + "ms");
                                    System.out.println("- First Byte Latency: " + metrics.getInt("firstByteLatency") + "ms");
                                    System.out.println("- Cache Read Input Token Count: " + metrics.getInt("cacheReadInputTokenCount"));
                                    System.out.println("- Cache Write Input Token Count: " + metrics.getInt("cacheWriteInputTokenCount"));
                                }
                                
                                // Parse any additional metrics if present
                                if (response.getJSONObject("metadata").has("metrics") && 
                                    !response.getJSONObject("metadata").getJSONObject("metrics").isEmpty()) {
                                    System.out.println("\nAdditional Metrics:");
                                    JSONObject additionalMetrics = response.getJSONObject("metadata").getJSONObject("metrics");
                                    for (String key : additionalMetrics.keySet()) {
                                        System.out.println("- " + key + ": " + additionalMetrics.get(key));
                                    }
                                }
                                
                                // Parse trace information if present
                                if (response.getJSONObject("metadata").has("trace") && 
                                    !response.getJSONObject("metadata").getJSONObject("trace").isEmpty()) {
                                    System.out.println("\nTrace Information:");
                                    JSONObject trace = response.getJSONObject("metadata").getJSONObject("trace");
                                    for (String key : trace.keySet()) {
                                        System.out.println("- " + key + ": " + trace.get(key));
                                    }
                                }
                                
                                System.out.println("===========================");
                            }
                            // You can add additional parsing for messageStart, messageStop, etc. if needed
                            
                        } catch (Exception e) {
                            System.err.println("Error processing response chunk: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }).build()).build();

            // Send the request and wait for the handler to process the response.
            client.invokeModelWithResponseStream(request, responseStreamHandler).get();
            
            System.out.println("\n--- Complete response received ---");
            
            // Return the complete response text.
            return completeResponseTextBuffer.toString();

        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Can't invoke model: " + e.getCause().getMessage());
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    public static void main(String[] args) {
        try {
            String result = invokeModelWithResponseStream();
            System.out.println("\nFinal result: " + result);
        } catch (Exception e) {
            System.err.println("Error in main: " + e.getMessage());
            e.printStackTrace();
        }
    }
}