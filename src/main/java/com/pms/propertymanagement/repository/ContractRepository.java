package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.Contract;
import com.pms.propertymanagement.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Page<Contract> findByRoom_Property_Owner_Id(Long ownerId, Pageable pageable);
    
    @Query("SELECT c FROM Contract c WHERE c.room.property.owner.id = :ownerId AND (:status IS NULL OR c.status = :status)")
    Page<Contract> findByOwnerIdAndStatus(Long ownerId, ContractStatus status, Pageable pageable);

    @Query("SELECT c FROM Contract c WHERE c.room.property.owner.id = :ownerId AND (:status IS NULL OR c.status = :status) AND (c.code LIKE %:keyword% OR c.room.name LIKE %:keyword% OR c.representative.fullName LIKE %:keyword%)")
    Page<Contract> searchByOwnerIdAndStatusAndKeyword(Long ownerId, ContractStatus status, String keyword, Pageable pageable);
}
