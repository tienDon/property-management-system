package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.repository.RoleRepository;
import com.pms.propertymanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public void init() {
        createAdmin();
        createOwner();
    }

    private void createAdmin() {
        if (userRepository.findByUsername("admin").isPresent()) return;

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("123");
        admin.setFullName("system admin");
        admin.getRoles().add(roleRepository.findByName("ADMIN").get());

        userRepository.save(admin);
    }

    private void createOwner() {
        if (userRepository.findByUsername("owner1").isPresent()) return;

        User owner = new User();
        owner.setUsername("owner1");
        owner.setPassword("123");
        owner.setFullName("owner1");
        owner.setPhone("0975893248");
        owner.setEmail("don@gmail.com");
        owner.getRoles().add(roleRepository.findByName("OWNER").get());

        userRepository.save(owner);
    }
}

