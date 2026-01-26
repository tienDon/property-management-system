package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.repository.UserRepository;
import com.pms.propertymanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User authenticate(String username, String password) {
        System.out.println("UserServiceImpl authenticate" + username + password);
        return userRepository.findByUsername(username).filter(user -> user.getPassword().equals(password)).orElse(null);
    }
}
