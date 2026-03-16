package com.pms.propertymanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadImageResult {
    private String hash;
    private String cloudinaryUrl;
    private String cloudinaryPublicId;
}

