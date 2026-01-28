package com.pms.propertymanagement.dto.response;

import com.pms.propertymanagement.enums.Gender;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String citizenId;
    private Gender gender;
    private String career;
    private String formattedBirthday;
    private String permanentAddress;
    private String formattedCreatedAt;
}
