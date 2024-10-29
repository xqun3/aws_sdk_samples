package org.example;

import java.math.BigDecimal;
import java.math.BigInteger;
// package com.example.bedrockruntime.models.anthropicClaude;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;
import software.amazon.awssdk.services.bedrockruntime.model.ToolSpecification;
import software.amazon.awssdk.services.bedrockruntime.model.ToolInputSchema;
import software.amazon.awssdk.services.bedrockruntime.model.ToolConfiguration;
import software.amazon.awssdk.services.bedrockruntime.model.Tool;
import software.amazon.awssdk.core.document.Document;


public class Handler {
    private final BedrockRuntimeClient bedrockRuntimeClient;

    public Handler() {
        bedrockRuntimeClient = DependencyFactory.bedrockRuntimeClient();
    }

	private Tool createWeatherQueryTool() throws JsonProcessingException {
		String schema = "{\"type\":\"object\",\"properties\":{\"latitude\":{\"type\":\"string\",\"description\":\"纬度\"},\"longitude\":{\"type\":\"string\",\"description\":\"经度\"}},\"required\":[\"latitude\",\"longitude\"]}";
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> schemaMap = objectMapper.readValue(schema, Map.class);

		ToolSpecification toolSpec = ToolSpecification.builder()
				.name("queryWeather")
				.description("queryWeather")
				.inputSchema(ToolInputSchema.builder().json(convertObjectToDocument(schemaMap)).build())
				.build();

		return Tool.fromToolSpec(toolSpec);
	}

    public String converse(BedrockRuntimeClient bedrockRuntimeClient) {

        // Create a Bedrock Runtime client in the AWS Region you want to use.
        // Replace the DefaultCredentialsProvider with your preferred credentials provider.


        // Set the model ID, e.g., Claude 3 Haiku.
        String modelId = "anthropic.claude-3-haiku-20240307-v1:0";

        // Create the input text and embed it in a message object with the user role.
		// String inputText = "Describe the purpose of a 'hello world' program in one line.";
		String inputText = "查询今天的天气";
        Message message = Message.builder()
                .content(ContentBlock.fromText(inputText))
                .role(ConversationRole.USER)
                .build();

        try {
			Tool tool = createWeatherQueryTool();
            // Send the message with a basic inference configuration.
            ConverseResponse response = bedrockRuntimeClient.converse(request -> request
                    .modelId(modelId)
                    .messages(message)
                    .toolConfig(ToolConfiguration.builder().tools(tool).build())
                    .inferenceConfig(config -> config
                            .maxTokens(512)
                            .temperature(0.5F)
                            .topP(0.9F)));

            // Retrieve the generated text from Bedrock's response object.
            String responseText = response.output().message().content().get(0).text();
			for(int i=0; i < response.output().message().content().size(); i++){
				System.out.println(response.output().message().content().get(i).type());
				System.out.println(response.output().message().content().get(i).toolUse());
			}

			System.out.println(responseText);

            return responseText;

        } catch (SdkClientException e) {
            System.err.printf("ERROR: Can't invoke '%s'. Reason: %s", modelId, e.getMessage());
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
			System.err.println("ERROR: Failed to create weather query tool");
			throw new RuntimeException("Failed to create weather query tool", e);
		}
    }


    @SuppressWarnings("unchecked")
	public static Document convertObjectToDocument(Object value) {
		if (value == null) {
			return Document.fromNull();
		} else if (value instanceof String) {
			return Document.fromString((String) value);
		} else if (value instanceof Boolean) {
			return Document.fromBoolean((Boolean) value);
		} else if (value instanceof Integer) {
			return Document.fromNumber((Integer) value);
		} else if (value instanceof Long) {
			return Document.fromNumber((Long) value);
		} else if (value instanceof Float) {
			return Document.fromNumber((Float) value);
		} else if (value instanceof Double) {
			return Document.fromNumber((Double) value);
		} else if (value instanceof BigDecimal) {
			return Document.fromNumber((BigDecimal) value);
		} else if (value instanceof BigInteger) {
			return Document.fromNumber((BigInteger) value);
		} else if (value instanceof List) {
			List<Document> documentList = ((List<?>) value).stream()
					.map(Handler::convertObjectToDocument)
					.collect(Collectors.toList());
			return Document.fromList(documentList);
		} else if (value instanceof Map) {
			return convertMapToDocument((Map<String, Object>) value);
		} else {
			throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getSimpleName());
		}
	}
	private static Document convertMapToDocument(Map<String, Object> value) {
		Map<String, Document> attr = value.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey(), e -> convertObjectToDocument(e.getValue())));
		return Document.fromMap(attr);
	}

    public void sendRequest() {
        converse(bedrockRuntimeClient);
    }
}
