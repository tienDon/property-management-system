package com.pms.propertymanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ClassifyIdResponse {
    private String message;
    private String status;
    private String statusCode;
    private List<String> errors;
    
    @JsonProperty("object")
    private ClassifyResult result;

    @Data
    public static class ClassifyResult {
        private Integer type;
        private String name;
    }
}
