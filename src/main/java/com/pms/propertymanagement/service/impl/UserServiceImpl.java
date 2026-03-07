package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.repository.UserRepository;
import com.pms.propertymanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User authenticate(String username, String password) {
        System.out.println("UserServiceImpl authenticate" + username + password);
        return userRepository.findByUsername(username).filter(user -> user.getPassword().equals(password)).orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public java.util.List<User> getUsersByRole(String roleName) {
        return userRepository.findByRoles_Name(roleName);
    }
}
