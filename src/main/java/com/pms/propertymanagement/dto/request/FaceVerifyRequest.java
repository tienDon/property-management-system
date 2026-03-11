package com.pms.propertymanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FaceVerifyRequest {
    private String img;
    private String unit;
    
    @JsonProperty("id_card")
    private String idCard;
    
    @JsonProperty("id_type")
    private String idType;
}
