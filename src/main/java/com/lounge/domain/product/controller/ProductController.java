package com.lounge.domain.product.controller;

import com.lounge.domain.product.dto.response.ProductListResponse;
import com.lounge.domain.product.dto.response.ProductResponse;
import com.lounge.domain.product.exception.code.ProductSuccessCode;
import com.lounge.domain.product.service.ProductService;
import com.lounge.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Product", description = "상품 API")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<List<ProductListResponse>> getProducts() {
        return ApiResponse.onSuccess(ProductSuccessCode.PRODUCT_LIST_SUCCESS, productService.getProducts());
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(
            @PathVariable Long productId
    ) {
        return ApiResponse.onSuccess(ProductSuccessCode.PRODUCT_DETAIL_SUCCESS, productService.getProduct(productId));
    }
}
