package org.example;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DependencyFactory {

    private DependencyFactory() {}

    public static SageMakerClient sageMakerClient() {
        return SageMakerClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    public static SageMakerRuntimeClient sageMakerRuntimeClient() {
        return SageMakerRuntimeClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    private static StaticCredentialsProvider getCredentialsProvider() {
        Properties prop = loadProperties();
        String accessKeyId = prop.getProperty("aws.accessKeyId");
        String secretAccessKey = prop.getProperty("aws.secretAccessKey");

        if (accessKeyId == null || secretAccessKey == null) {
            throw new IllegalStateException("AWS credentials not set in config.properties");
        }

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return StaticCredentialsProvider.create(awsCreds);
    }

    private static Region getRegion() {
        Properties prop = loadProperties();
        String awsRegion = prop.getProperty("aws.region");
        if (awsRegion == null) {
            throw new IllegalStateException("AWS region not set in config.properties");
        }

        return Region.of(awsRegion);
    }

    private static Properties loadProperties() {
        Properties prop = new Properties();
        try (InputStream input = DependencyFactory.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IllegalStateException("Unable to find config.properties");
            }
            prop.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config.properties", e);
        }
        return prop;
    }
}
