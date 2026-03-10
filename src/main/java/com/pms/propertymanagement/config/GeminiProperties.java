package com.pms.propertymanagement.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds application.properties prefix "gemini":
 *   gemini.api.key, gemini.api.url
 *   gemini.timeout.connect, gemini.timeout.read
 */
@Component
@ConfigurationProperties(prefix = "gemini")
@Getter
@Setter
public class GeminiProperties {

    private Api api = new Api();
    private Timeout timeout = new Timeout();

    @Getter
    @Setter
    public static class Api {
        private String key;
        private String url;
    }

    @Getter
    @Setter
    public static class Timeout {
        /** Connect timeout in milliseconds */
        private int connect = 5000;
        /** Read timeout in milliseconds */
        private int read = 30000;
    }
}
