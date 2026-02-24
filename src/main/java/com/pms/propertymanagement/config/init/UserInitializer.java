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
        createOwner1();
        createTenant1();
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

    private void createOwner1() {
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

    private void createTenant1() {
        if (userRepository.findByUsername("tenant1").isPresent()) return;

        User tenant = new User();
        tenant.setUsername("tenant1");
        tenant.setPassword("123");
        tenant.setFullName("tenant1");
        tenant.setPhone("0900000001");
        tenant.setEmail("tenant1@gmail.com");
        tenant.getRoles().add(roleRepository.findByName("USER").get());

        userRepository.save(tenant);
    }

    private void createOwner2() {
        if (userRepository.findByUsername("owner2").isPresent()) return;

        User owner = new User();
        owner.setUsername("owner2");
        owner.setPassword("123");
        owner.setFullName("owner2");
        owner.setPhone("0909090909");
        owner.setEmail("kiet@gmail.com");
        owner.getRoles().add(roleRepository.findByName("OWNER").get());

        userRepository.save(owner);
    }

    private void createOwner3() {
        if (userRepository.findByUsername("owner3").isPresent()) return;

        User owner = new User();
        owner.setUsername("owner3");
        owner.setPassword("123");
        owner.setFullName("owner3");
        owner.setPhone("0707070707");
        owner.setEmail("thach@gmail.com");
        owner.getRoles().add(roleRepository.findByName("OWNER").get());

        userRepository.save(owner);
    }

    private void createOwner4() {
        if (userRepository.findByUsername("owner4").isPresent()) return;

        User owner = new User();
        owner.setUsername("owner4");
        owner.setPassword("123");
        owner.setFullName("owner4");
        owner.setPhone("0975893248");
        owner.setEmail("an@gmail.com");
        owner.getRoles().add(roleRepository.findByName("OWNER").get());

        userRepository.save(owner);
    }
}

