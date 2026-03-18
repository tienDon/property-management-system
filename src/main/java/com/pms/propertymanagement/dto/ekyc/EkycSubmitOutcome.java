package com.pms.propertymanagement.dto.ekyc;

import com.pms.propertymanagement.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class EkycSubmitOutcome {
    private boolean success;
    private String redirectUrl;
    private String next;
    private List<String> errors;
    private User user;
}
