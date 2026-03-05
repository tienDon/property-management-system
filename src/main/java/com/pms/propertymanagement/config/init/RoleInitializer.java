package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.Role;
import com.pms.propertymanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer {

    private final RoleRepository roleRepository;

    public void init() {
        createIfNotExists("ADMIN");
        createIfNotExists("USER");
        createIfNotExists("OWNER");
        createIfNotExists("STAFF");
        createIfNotExists("MODERATOR");
    }

    private void createIfNotExists(String name) {
        roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    return roleRepository.save(role);
                });
    }
}

