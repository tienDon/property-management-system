package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//dieu khoan hop dong

@Entity
@Table(name = "contract_terms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractTerm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @Column(name = "term_title")
    @NotBlank(message = "Title is required")
    private String termTitle;

    @Column(name = "term_content")
    @NotBlank(message = "Content is required")
    private String termContent;

    @Column(name = "order_index")
    private Integer orderIndex = 0; //thu tu hien thi

}
