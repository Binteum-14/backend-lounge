package com.lounge.domain.ownerproduct.repository;

import com.lounge.domain.ownerproduct.entity.OwnedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OwnedProductRepository extends JpaRepository<OwnedProduct, Long> {

    @Query("""
            select op from OwnedProduct op
            join fetch op.product
            where op.user.id = :userId
            order by op.id desc
            """)
    List<OwnedProduct> findAllByUserIdWithProduct(
            @Param("userId") Long userId
    );

    @Query("""
            select op from OwnedProduct op
            join fetch op.product
            where op.id = :ownedProductId
              and op.user.id = :userId
            """)
    Optional<OwnedProduct> findByIdAndUserIdWithProduct(
            @Param("ownedProductId") Long ownedProductId,
            @Param("userId") Long userId
    );

    void deleteByUser_Id(Long userId);
}
