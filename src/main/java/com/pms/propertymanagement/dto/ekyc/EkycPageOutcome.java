package com.pms.propertymanagement.dto.ekyc;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EkycPageOutcome {
    private boolean redirect;
    private String redirectUrl;
    private String next;
    private LocalDateTime lastSubmissionAt;
}
