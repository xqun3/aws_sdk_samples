package org.example;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String... args) {
        logger.info("Application starts - SageMaker endpoint invocation");

        Handler handler = new Handler();

        // List available endpoints
        handler.listEndpoints();

        // Example 1: Invoke the first available endpoint with a default query
        handler.sendRequest(null, null);

        // Example 2: Invoke a specific endpoint with a custom query
        handler.sendRequest("your-endpoint-name", "Custom query text");

        // Example 3: Invoke the first available endpoint with a custom query
        handler.sendRequest(null, "Another custom query");

        logger.info("Application ends - SageMaker endpoint invocation complete");
    }
}
