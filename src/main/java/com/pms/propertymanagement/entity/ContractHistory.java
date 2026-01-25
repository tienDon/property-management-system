package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//lich su thay doi hop dong
@Entity
@Table(name = "contract_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;

    private String action; // CREATED, APPROVED, ACTIVATED, TERMINATED

    @ManyToOne
    @JoinColumn(name = "performed_by")
    private User performedBy; //nguoi thuc hien

    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
