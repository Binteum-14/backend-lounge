package com.ddcj.binteum.domain.visitpassproduct.entity;

import com.ddcj.binteum.domain.product.entity.Product;
import com.ddcj.binteum.domain.visitpass.entity.VisitPass;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "visit_pass_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VisitPassProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visit_pass_id", nullable = false)
    private VisitPass visitPass;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
