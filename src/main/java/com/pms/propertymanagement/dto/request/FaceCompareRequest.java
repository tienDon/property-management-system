package com.pms.propertymanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FaceCompareRequest {
    @JsonProperty("img_front")
    private String imgFront;
    
    @JsonProperty("img_face")
    private String imgFace;
    
    @JsonProperty("client_session")
    private String clientSession;
    
    private String token;
}
