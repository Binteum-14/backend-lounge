package com.lounge.domain.ownerproduct.entity;

import com.lounge.domain.product.entity.Product;
import com.lounge.domain.user.entity.User;
import com.lounge.global.entity.CreatedAtEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "owned_product",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_owned_product_user_product",
                columnNames = {"user_id", "product_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OwnedProduct extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private OwnedProduct(User user, Product product) {
        this.user = user;
        this.product = product;
    }

    public static OwnedProduct createVerified(User user, Product product) {
        return new OwnedProduct(user, product);
    }
}
