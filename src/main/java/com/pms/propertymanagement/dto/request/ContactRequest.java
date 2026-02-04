package com.pms.propertymanagement.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
public class ContactRequest
{
    private String name;
    private String phone;
    private String note;
}
