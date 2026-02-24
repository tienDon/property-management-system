package com.pms.propertymanagement.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterForm {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
}

