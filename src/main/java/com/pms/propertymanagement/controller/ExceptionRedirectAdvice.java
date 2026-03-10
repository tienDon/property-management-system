package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.exception.ForbiddenException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionRedirectAdvice {
    @ExceptionHandler(ForbiddenException.class)
    public String handleForbidden() {
        return "redirect:/403";
    }
}
