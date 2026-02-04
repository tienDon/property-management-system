package com.pms.propertymanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ContactResponse {
    private Long id;
    private String name;
    private String phone;
    private String note;
    private String propertyTitle;
    private String propertySlug;
    private Boolean isChecked;
}
