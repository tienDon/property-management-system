package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "viewing_appointment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewingAppointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private User tenant;
    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;
    @Column(name = "appointment_date")
    @NotNull(message = "Data is required")
    @FutureOrPresent(message = "Date must be today or furture")
    private LocalDate appointmentDate;//ngay hen
    @Column(name = "appointment_time")
    @NotNull(message = "Time is required")
    private LocalTime appointmentTime;//gio hen

    private String status = "PENDING"; // PENDING, CONFIRMED, CANCELLED, COMPLETED
    private String notes;
    @Column(name = "contact_phone")
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone must be 10-11 digits")
    private String contactPhone;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
