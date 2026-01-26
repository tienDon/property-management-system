package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.User;

public interface UserService {
    User authenticate(String username, String password);
}
