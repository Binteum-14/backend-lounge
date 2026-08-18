package com.lounge.domain.product.repository;

import com.lounge.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByActiveTrue();

    List<Product> findTop3ByOrderByIdAsc();

    Optional<Product> findBySku(String sku);
}
