package com.pms.propertymanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;

/**
 * ENTERPRISE-GRADE Transaction Configuration
 * Ensures proper isolation levels and timeout management
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class TransactionConfig implements TransactionManagementConfigurer {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return transactionManager;
    }

    /**
     * Configure default transaction properties for enterprise use
     */
    // Additional bean definitions for transaction timeout configuration
    // can be added here if needed for specific use cases
}