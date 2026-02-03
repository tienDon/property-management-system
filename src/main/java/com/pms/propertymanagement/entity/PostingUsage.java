package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "posting_usages", indexes = {
        @Index(name = "idx_posting_usages_order", columnList = "order_id"),
        @Index(name = "idx_posting_usages_property", columnList = "property_id")
})
@Getter
@Setter
public class PostingUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PostingOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    private LocalDateTime usedAt = LocalDateTime.now();
}
