package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.pms.propertymanagement.enums.AccountType;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", columnDefinition = "nvarchar(255)")
    private String fullName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;
    
    @Column(name = "card_id")
    private String cardId;
    
    @Column(name = "dob")
    private String dob;
    
    @Column(name = "address", columnDefinition = "nvarchar(500)")
    private String address;
    
    @Column(name = "hometown", columnDefinition = "nvarchar(500)")
    private String hometown;
    
    @Column(name = "gender")
    private String gender;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "is_ekyc_verified")
    private Boolean ekycVerified = false;

    @Column(name = "ekyc_verified_at")
    private LocalDateTime ekycVerifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    private AccountType accountType;

    @Column(name="create_at", updatable = false)
    private LocalDateTime createAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "owner")
    private Set<Property> properties = new HashSet<>();

    @OneToMany(mappedBy = "owner")
    private Set<Contact> contacts = new HashSet<>();


    @PrePersist
    protected void onCreate(){
        this.createAt = LocalDateTime.now();
    }

    public boolean isEkycVerified() {
        return Boolean.TRUE.equals(ekycVerified);
    }
}
