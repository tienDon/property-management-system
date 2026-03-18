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
        createOwner2();
        createOwner3();
        createOwner4();
        createTenant1();
        createStaff1();
        createModerator1();
    }

    private void createAdmin() {
        if (userRepository.findByUsername("admin").isPresent()) return;

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("123");
        admin.setFullName("system admin");
        admin.setEkycVerified(true);
        admin.getRoles().add(roleRepository.findByName("ADMIN").get());

        userRepository.save(admin);
    }

    private void createOwner1() {
        var existing = userRepository.findByUsername("owner1");
        if (existing.isPresent()) {
            ensureRole(existing.get(), "OWNER");
            return;
        }

        User owner = new User();
        owner.setUsername("owner1");
        owner.setPassword("123");
        owner.setFullName("owner1");
        owner.setPhone("0975893248");
        owner.setEmail("don@gmail.com");
        owner.setEkycVerified(true);
        owner.getRoles().add(roleRepository.findByName("OWNER").get());

        userRepository.save(owner);
    }

    private void createTenant1() {
        var existing = userRepository.findByUsername("tenant1");
        if (existing.isPresent()) {
            ensureRole(existing.get(), "USER");
            return;
        }

        User tenant = new User();
        tenant.setUsername("tenant1");
        tenant.setPassword("123");
        tenant.setFullName("Nguyễn Văn An");
        tenant.setPhone("0901234567");
        tenant.setEmail("nva@gmail.com");
        tenant.setEkycVerified(true);
        tenant.getRoles().add(roleRepository.findByName("USER").get());

        userRepository.save(tenant);
    }

    private void createStaff1() {
        var existing = userRepository.findByUsername("staff1");
        if (existing.isPresent()) {
            ensureRole(existing.get(), "STAFF");
            return;
        }

        User staff = new User();
        staff.setUsername("staff1");
        staff.setPassword("123");
        staff.setFullName("Staff 1");
        staff.setPhone("0901111111");
        staff.setEmail("staff1@gmail.com");
        staff.setEkycVerified(true);
        staff.getRoles().add(roleRepository.findByName("STAFF").get());

        userRepository.save(staff);
    }

    private void ensureRole(User user, String roleName) {
        boolean hasRole = user.getRoles() != null && user.getRoles().stream().anyMatch(r -> roleName.equals(r.getName()));
        if (hasRole) return;
        user.getRoles().add(roleRepository.findByName(roleName).orElseThrow());
        userRepository.save(user);
    }

    private void createModerator1() {
        if (userRepository.findByUsername("moderator1").isPresent()) return;

        User mod = new User();
        mod.setUsername("moderator1");
        mod.setPassword("123");
        mod.setFullName("Kiểm duyệt viên 1");
        mod.setPhone("0900000099");
        mod.setEmail("mod1@tromoi.com");
        mod.setEkycVerified(true);
        mod.getRoles().add(roleRepository.findByName("MODERATOR").get());

        userRepository.save(mod);
    }

    private void createOwner2() {
        if (userRepository.findByUsername("owner2").isPresent()) return;

        User owner = new User();
        owner.setUsername("owner2");
        owner.setPassword("123");
        owner.setFullName("owner2");
        owner.setPhone("0909090909");
        owner.setEmail("kiet@gmail.com");
        owner.setEkycVerified(true);
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
        owner.setEkycVerified(true);
        owner.getRoles().add(roleRepository.findByName("OWNER").get());

        userRepository.save(owner);
    }

    private void createOwner4() {
        if (userRepository.findByUsername("owner4").isPresent()) return;

        User owner = new User();
        owner.setUsername("owner4");
        owner.setPassword("123");
        owner.setFullName("owner4");
        owner.setPhone("0808080808");
        owner.setEmail("an@gmail.com");
        owner.setEkycVerified(true);
        owner.getRoles().add(roleRepository.findByName("OWNER").get());

        userRepository.save(owner);
    }
}

