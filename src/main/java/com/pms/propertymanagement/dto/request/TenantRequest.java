package com.pms.propertymanagement.dto.request;

import com.pms.propertymanagement.enums.Gender;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class TenantRequest {
    private String fullName;
    private String phone;
    private String email;
    private String citizenId;
    private Gender gender;
    private String career;
    private String permanentAddress;
    private String placeOfIssue;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;
}
