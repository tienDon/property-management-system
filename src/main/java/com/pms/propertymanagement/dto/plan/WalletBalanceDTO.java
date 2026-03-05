package com.pms.propertymanagement.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceDTO {
    private BigDecimal balance;
    private String displayBalance;
    private Boolean isLowBalance; // < 100k
    private Boolean canAffordBasic; // >= 99k
    private Boolean canAffordPro; // >= 179k
    private Boolean canAffordPremium; // >= 299k
}