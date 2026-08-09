package com.ddcj.binteum.domain.product.repository;

import com.ddcj.binteum.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
