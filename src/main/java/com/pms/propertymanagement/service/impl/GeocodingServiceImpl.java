package com.pms.propertymanagement.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.propertymanagement.config.GeocodingProperties;
import com.pms.propertymanagement.dto.chat.GeocodingResult;
import com.pms.propertymanagement.service.GeocodingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Calls OpenCage Geocoding API to convert a place name into coordinates.
 *
 * API docs: https://opencagedata.com/api
 * Free tier: 2500 req/day, no billing required.
 *
 * Response fields used:
 *   results[0].geometry.lat / .lng
 *   results[0].confidence          (1-10)
 *   results[0].components.suburb   (ward name hint, may be absent)
 *   results[0].formatted           (normalized address string)
 *   status.code                    (200 = ok)
 *
 * Returns null when:
 *   - API returns non-200 status
 *   - results array is empty
 *   - confidence < geocode.confidence.min (default 5)
 *   - _type == "bus_stop" (low-quality result)
 *   - Any network/parse exception
 */
@Service
@Slf4j
public class GeocodingServiceImpl implements GeocodingService {

    private final GeocodingProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient;

    public GeocodingServiceImpl(GeocodingProperties props) {
        this.props = props;
        // Apply connect + read timeouts from application.properties
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.getTimeout().getConnect()))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(props.getTimeout().getRead()));
        this.restClient = RestClient.builder()
                .baseUrl(props.getApi().getUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public GeocodingResult geocode(String placeName, String provinceName) {
        // Build disambiguated query: "Đại học FPT, Thành phố Hồ Chí Minh, Việt Nam"
        String query = placeName + ", " + provinceName + ", Việt Nam";

        String responseBody;
        try {
            responseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("q", query)
                            .queryParam("key", props.getApi().getKey())
                            .queryParam("language", "vi")
                            .queryParam("countrycode", "vn")
                            .queryParam("limit", "1")
                            .queryParam("no_annotations", "1")
                            .build())
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            log.warn("Geocoding API call failed for query '{}': {}", query, e.getMessage());
            return null;
        }

        if (responseBody == null) {
            log.warn("Geocoding API returned null body for query '{}'", query);
            return null;
        }

        try {
            return parseResponse(responseBody, query);
        } catch (Exception e) {
            log.warn("Failed to parse geocoding response for query '{}': {}", query, e.getMessage());
            return null;
        }
    }

    private GeocodingResult parseResponse(String responseBody, String query) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        // Check API status
        int statusCode = root.path("status").path("code").asInt(0);
        if (statusCode != 200) {
            log.warn("Geocoding API returned status {} for query '{}'", statusCode, query);
            return null;
        }

        JsonNode results = root.path("results");
        if (!results.isArray() || results.isEmpty()) {
            log.debug("Geocoding API returned no results for query '{}'", query);
            return null;
        }

        JsonNode first = results.get(0);
        int confidence = first.path("confidence").asInt(0);

        // Reject low-confidence results
        if (confidence < props.getConfidence().getMin()) {
            log.debug("Geocoding confidence {} below min {} for query '{}'",
                    confidence, props.getConfidence().getMin(), query);
            return null;
        }

        // Reject clearly wrong result types
        String type = first.path("components").path("_type").asText("");
        if ("bus_stop".equals(type)) {
            log.debug("Geocoding rejected bus_stop result for query '{}'", query);
            return null;
        }

        double lat = first.path("geometry").path("lat").asDouble();
        double lng = first.path("geometry").path("lng").asDouble();
        String suburb = first.path("components").path("suburb").asText(null);
        // "suburb" key may be absent → asText(null) returns null safely
        if (suburb != null && suburb.isBlank()) {
            suburb = null;
        }
        String normalizedAddress = first.path("formatted").asText(null);

        log.debug("Geocoded '{}' → lat={}, lng={}, confidence={}", query, lat, lng, confidence);
        return new GeocodingResult(lat, lng, confidence, suburb, normalizedAddress);
    }
}
