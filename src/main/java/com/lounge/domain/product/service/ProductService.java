package com.lounge.domain.product.service;

import com.lounge.domain.product.dto.response.ProductListResponse;
import com.lounge.domain.product.dto.response.ProductResponse;
import com.lounge.domain.product.exception.ProductException;
import com.lounge.domain.product.exception.code.ProductErrorCode;
import com.lounge.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductListResponse> getProducts() {
        return productRepository.findTop3ByOrderByIdAsc().stream()
                .map(ProductListResponse::from)
                .toList();
    }

    public ProductResponse getProduct(Long productId) {
        return productRepository.findById(productId)
                .map(ProductResponse::from)
                .orElseThrow(() -> ProductException.of(ProductErrorCode.PRODUCT_NOT_FOUND));
    }
}
