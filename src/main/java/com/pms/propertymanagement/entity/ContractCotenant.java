package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//Nguoi  o cung
@Entity
@Table(name = "contract_cotenants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractCotenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @Column(name = "full_name")
    @NotBlank(message = "Name is required")
    private String fullName;

    @Column(name = "id_number")
    @Pattern(regexp = "^[0-9]{9,12}$", message = "ID must be 9-12 digits")
    private String idNumber; // CMND/CCCD

    private String phone;
    private String relationship;
}
