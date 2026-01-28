package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.ContractDTO;
import com.pms.propertymanagement.entity.Contract;
import com.pms.propertymanagement.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContractService {
    Contract createContract(ContractDTO contractDTO);
    Page<Contract> getContractsByOwner(Long ownerId, ContractStatus status, String keyword, Pageable pageable);
    Contract getContractById(Long id);
    void updateContractStatus(Long id, ContractStatus status);
}
