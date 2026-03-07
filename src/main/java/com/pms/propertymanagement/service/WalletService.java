package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.entity.UserWallet;
import com.pms.propertymanagement.entity.WalletTransaction;
import com.pms.propertymanagement.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface WalletService {
    
    // Wallet management
    UserWallet getOrCreateWallet(User user);
    UserWallet getWalletByUser(User user);
    BigDecimal getCurrentBalance(User user);
    
    // Transaction operations
    WalletTransaction deposit(User user, BigDecimal amount, String description, String vnpayTxnRef);
    WalletTransaction deduct(User user, BigDecimal amount, TransactionType type, String description, String referenceId);
    WalletTransaction refund(User user, BigDecimal amount, String description, String referenceId);
    
    // Transaction history
    Page<WalletTransaction> getTransactionHistory(User user, Pageable pageable);
    List<WalletTransaction> getRecentTransactions(User user, int limit);
    
    // Statistics
    BigDecimal getTotalDeposited(User user);
    BigDecimal getTotalSpent(User user);
    BigDecimal getMonthlySpending(User user);
    
    // Validation
    boolean hasEnoughBalance(User user, BigDecimal amount);
    
    // VNPay integration
    String createDepositUrl(User user, BigDecimal amount, String returnUrl, String ipAddress);
    boolean processVnpayCallback(Map<String, String> returnParams, String rawQuery);
}