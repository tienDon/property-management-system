package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.entity.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {
    
    Optional<UserWallet> findByUser(User user);
    
    Optional<UserWallet> findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);
}