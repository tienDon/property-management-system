package com.pms.propertymanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class OcrBackResponse {
    private String message;
    
    @JsonProperty("server_version")
    private String serverVersion;
    
    @JsonProperty("object")
    private OcrBackResult result;
    
    private String status;
    private String statusCode;
    private List<String> errors;

    @Data
    public static class OcrBackResult {
        @JsonProperty("issue_place")
        private String issuePlace;
        
        @JsonProperty("issue_date_prob")
        private Float issueDateProb;
        
        @JsonProperty("issue_place_prob")
        private Float issuePlaceProb;
        
        @JsonProperty("issue_date")
        private String issueDate;
        
        @JsonProperty("issue_date_probs")
        private List<Float> issueDateProbs;
        
        @JsonProperty("back_type_id")
        private Integer backTypeId;
        
        @JsonProperty("back_expire_warning")
        private String backExpireWarning;
        
        @JsonProperty("msg_back")
        private String msgBack;
        
        @JsonProperty("card_type")
        private String cardType;
        
        @JsonProperty("recent_location_prob")
        private Float recentLocationProb;
        
        @JsonProperty("warning")
        private List<String> warning;
        
        @JsonProperty("warning_msg")
        private List<String> warningMsg;
        
        @JsonProperty("expire_warning")
        private String expireWarning;
    }
}
