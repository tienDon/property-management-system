package com.pms.propertymanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FaceMaskRequest {
    private String img;
    
    @JsonProperty("face_bbox")
    private String faceBbox;
    
    @JsonProperty("face_lmark")
    private String faceLmark;
    
    @JsonProperty("client_session")
    private String clientSession;
}
