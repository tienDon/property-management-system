package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User authenticate(String username, String password);
    User findByUsername(String username);
    Optional<User> findById(Long id);

    /**
     * Get all users with the given role name
     */
    List<User> getUsersByRole(String roleName);
}
