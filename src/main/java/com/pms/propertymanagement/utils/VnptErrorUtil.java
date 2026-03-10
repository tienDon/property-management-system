package com.pms.propertymanagement.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.HttpClientErrorException;

public class VnptErrorUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String extractErrorMessage(HttpClientErrorException e) {
        try {
            String body = e.getResponseBodyAsString();
            JsonNode node = mapper.readTree(body);
            
            if (node.has("errors") && node.get("errors").isArray() && node.get("errors").size() > 0) {
                return node.get("errors").get(0).asText();
            }
            
            if (node.has("message")) {
                return node.get("message").asText();
            }
        } catch (Exception parseEx) {
            // fallback to original message
        }
        return e.getMessage();
    }
}
