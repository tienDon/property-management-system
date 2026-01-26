package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.Contract;

public interface ContractService {
    Contract createContract(Contract contract);
    Contract approveContract(Long id, Long userId);
    Contract activateContract(Long id, Long userId);
    Contract getById(Long id);
}
