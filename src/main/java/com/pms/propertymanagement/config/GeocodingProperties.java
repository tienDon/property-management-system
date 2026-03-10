package com.pms.propertymanagement.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds application.properties prefix "geocode":
 *   geocode.api.key, geocode.api.url
 *   geocode.timeout.connect, geocode.timeout.read
 *   geocode.confidence.min
 *   geocode.radius.confident, geocode.radius.approximate
 */
@Component
@ConfigurationProperties(prefix = "geocode")
@Getter
@Setter
public class GeocodingProperties {

    private Api api = new Api();
    private Timeout timeout = new Timeout();
    private Confidence confidence = new Confidence();
    private Radius radius = new Radius();

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
        private int connect = 3000;
        /** Read timeout in milliseconds */
        private int read = 5000;
    }

    @Getter
    @Setter
    public static class Confidence {
        /** Minimum confidence score (1-10) to accept a geocoding result */
        private int min = 5;
    }

    @Getter
    @Setter
    public static class Radius {
        /** Search radius (km) when confidence >= 8 — building/POI level */
        private double confident = 3.0;
        /** Search radius (km) when confidence 5-7 — street/area level */
        private double approximate = 5.0;
    }
}
