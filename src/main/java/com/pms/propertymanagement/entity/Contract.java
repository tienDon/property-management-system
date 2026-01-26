package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_code", unique = true)
    private String contractCode;

    @ManyToOne
    @JoinColumn(name = "room_id")
    @NotNull(message = "Room is required")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    @NotNull(message = "Tenant is required")
    private User tenant;

    @Column(name = "start_date")
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or future")
    private LocalDate startDate;//ngay bat dau

    @Column(name = "end_date")
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in future")
    private LocalDate endDate;//ngay ket thuc

    @Column(name = "monthly_rent")
    @NotNull(message = "Monthly rent is required")
    @Positive(message = "Rent must be positive")
    private Double monthlyRent;//tien thue hang thang

    @Column(name = "deposit_amount")
    @NotNull(message = "Deposit is required")
    @PositiveOrZero(message = "Deposit must be zero or positive")
    private Double depositAmount; //Tien coc

    @Column(name = "electricity_price")
    private Double electricityPrice = 3500.0; //Tien diem đ/kWh

    @Column(name = "water_price")
    private Double waterPrice = 20000.0 ; //gia nuoc đ/m3

    @Column(name = "service_fee")
    private Double serviceFee = 0.0;//phi dich vu

    @Column(name = "internet_fee")
    private Double internetFee = 0.0;//phi Internet

    @Column(name = "parking_fee")
    private Double parkingFee = 0.0;//phi gui xe

    @Column(name = "contract_term_months")
    @Min(value = 6, message = "Term must be at least 6 month")
    @Max(value = 12, message = "Term cannot exceed 12 months")
    private Integer contractTermMonth;//Thoi han thue

    @Column(name = "payment_date")
    @Min(value = 28, message = "Payment date must be 28-31")
    @Max(value = 31, message = "Payment date must be 28-31")
    private Integer paymentDate;//Ngay thanh toan

    @Column(name = "payment_method")
    @Pattern(regexp = "CASH|BANK_TRANSFER", message = "Invalid payment method")
    private String paymentMethod;
    private String note;
    private String status = "PENDING"; // PENDING, APPROVED, ACTIVE, TERMINATED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;//nguoi tao hop dong

    @Nationalized
    private String historyNote;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // tu sinh neu hop dong chua co
        if (contractCode == null) {
            contractCode = "HD" + LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
