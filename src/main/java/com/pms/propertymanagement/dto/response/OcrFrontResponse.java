package com.pms.propertymanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class OcrFrontResponse {
    private String message;
    
    @JsonProperty("server_version")
    private String serverVersion;
    
    @JsonProperty("object")
    private OcrFrontResult result;
    
    private String status;
    private String statusCode;
    private List<String> errors;

    @Data
    public static class OcrFrontResult {
        @JsonProperty("name_prob")
        private Float nameProb;
        
        @JsonProperty("origin_location")
        private String originLocation;
        
        private String msg;
        
        @JsonProperty("birth_day_label")
        private String birthDayLabel;
        
        private String gender;
        
        @JsonProperty("recent_location_label")
        private String recentLocationLabel;
        
        @JsonProperty("expire_warning")
        private String expireWarning;
        
        @JsonProperty("nation_slogan")
        private String nationSlogan;
        
        @JsonProperty("valid_date_prob")
        private Float validDateProb;
        
        @JsonProperty("nation_policy")
        private String nationPolicy;
        
        @JsonProperty("origin_location_prob")
        private Float originLocationProb;
        
        @JsonProperty("valid_date")
        private String validDate;
        
        @JsonProperty("issue_date")
        private String issueDate;
        
        @JsonProperty("id_fake_prob")
        private Float idFakeProb;
        
        @JsonProperty("nationality_prob")
        private Float nationalityProb;
        
        private String id;
        
        @JsonProperty("citizen_id_prob")
        private Float citizenIdProb;
        
        @JsonProperty("id_probs")
        private String idProbs;
        
        @JsonProperty("issue_place")
        private String issuePlace;
        
        @JsonProperty("birth_day_prob")
        private Float birthDayProb;
        
        @JsonProperty("recent_location")
        private String recentLocation;
        
        @JsonProperty("id_fake_warning")
        private String idFakeWarning;
        
        @JsonProperty("type_id")
        private Integer typeId;
        
        @JsonProperty("card_type")
        private String cardType;
        
        @JsonProperty("name_label")
        private String nameLabel;
        
        @JsonProperty("birth_day")
        private String birthDay;
        
        @JsonProperty("issue_date_prob")
        private Float issueDateProb;
        
        @JsonProperty("citizen_id")
        private String citizenId;
        
        @JsonProperty("recent_location_prob")
        private Float recentLocationProb;
        
        @JsonProperty("issue_place_prob")
        private Float issuePlaceProb;
        
        @JsonProperty("gender_prob")
        private Float genderProb;
        
        private String nationality;
        
        @JsonProperty("post_code")
        private List<PostCode> postCode;
        
        private String name;
        
        private Tampering tampering;
        
        @JsonProperty("origin_location_label")
        private String originLocationLabel;
        
        @JsonProperty("warning")
        private List<String> warning;
        
        @JsonProperty("warning_msg")
        private List<String> warningMsg;
    }
    
    @Data
    public static class PostCode {
        private List<Object> city;
        private List<Object> district;
        private List<Object> ward;
        private String type;
    }
    
    @Data
    public static class Tampering {
        @JsonProperty("is_legal")
        private String isLegal;
        private List<String> warning;
    }
}
