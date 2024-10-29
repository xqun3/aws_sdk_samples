
package org.example;

import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

/**
 * The module containing all dependencies required by the {@link Handler}.
 */
public class DependencyFactory {

    private DependencyFactory() {}

    /**
     * @return an instance of BedrockRuntimeClient
     */
    public static BedrockRuntimeClient bedrockRuntimeClient() {
        return BedrockRuntimeClient.builder()
                       .httpClientBuilder(ApacheHttpClient.builder())
                       .build();
    }
}
