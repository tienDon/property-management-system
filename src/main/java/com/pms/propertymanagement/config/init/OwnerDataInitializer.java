package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.Subscription;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.entity.UserWallet;
import com.pms.propertymanagement.enums.SubscriptionStatus;
import com.pms.propertymanagement.enums.SubscriptionType;
import com.pms.propertymanagement.repository.ManagementPlanRepository;
import com.pms.propertymanagement.repository.SubscriptionRepository;
import com.pms.propertymanagement.repository.UserRepository;
import com.pms.propertymanagement.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Seeds sample data for owner1:
 *  - Wallet balance: 2,000,000 VNĐ
 *  - Active ENTERPRISE management subscription (30 days)
 */
@Component
@RequiredArgsConstructor
public class OwnerDataInitializer {

    private final UserRepository userRepository;
    private final UserWalletRepository walletRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ManagementPlanRepository managementPlanRepository;

    public void init() {
        User owner1 = userRepository.findByUsername("owner1").orElse(null);
        if (owner1 == null) return;

        initWallet(owner1);
        initEnterpriseSubscription(owner1);
    }

    // ── Wallet ────────────────────────────────────────────────────────────────
    private void initWallet(User owner) {
        UserWallet wallet = walletRepository.findByUserId(owner.getId()).orElse(null);

        if (wallet == null) {
            wallet = new UserWallet();
            wallet.setUser(owner);
        }

        // Only set if balance is still 0 (idempotent re-run)
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) == 0) {
            wallet.setBalance(new BigDecimal("2000000"));
            wallet.setTotalDeposited(new BigDecimal("2000000"));
        }

        walletRepository.save(wallet);
    }

    // ── Enterprise Subscription ───────────────────────────────────────────────
    private void initEnterpriseSubscription(User owner) {
        // Skip if owner already has an active management subscription
        boolean alreadyActive = subscriptionRepository
                .findByUserIdOrderByCreatedAtDesc(owner.getId())
                .stream()
                .anyMatch(s -> s.getType() == SubscriptionType.MANAGEMENT
                        && s.getStatus() == SubscriptionStatus.ACTIVE);

        if (alreadyActive) return;

        var enterprisePlan = managementPlanRepository.findByCode("ENTERPRISE").orElse(null);
        if (enterprisePlan == null) return;

        Subscription sub = new Subscription();
        sub.setUser(owner);
        sub.setManagementPlanId(enterprisePlan.getId());
        sub.setType(SubscriptionType.MANAGEMENT);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setStartedAt(LocalDateTime.now());
        sub.setExpiredAt(LocalDateTime.now().plusDays(30));

        subscriptionRepository.save(sub);
    }
}
