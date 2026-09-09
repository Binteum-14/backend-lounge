package com.lounge.domain.product.service;

import com.lounge.domain.packing.dto.PackingProfile;
import com.lounge.domain.packing.service.PackingProfileCatalog;
import com.lounge.domain.product.dto.response.ProductResponse;
import com.lounge.domain.product.entity.Product;
import com.lounge.domain.product.entity.ProductVariant;
import com.lounge.domain.product.repository.ProductRepository;
import com.lounge.domain.product.repository.ProductVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private PackingProfileCatalog packingProfileCatalog;

    @InjectMocks
    private ProductService productService;

    @Test
    void returnsThePackingProfileLinkedToTheSelectedProductSku() {
        Product product = mock(Product.class);
        ProductVariant selectedProduct = mock(ProductVariant.class);
        PackingProfile productFourProfile = new PackingProfile(
                "F04", "flight", "MWRFAXT01PZ001", "Tracy 비세토스 크로스바디",
                "Soft Pink", "가방/비행기/04_tracy_visetos_crossbody_soft_pink.png",
                8.0, 21.0, 15.0, false, null, false,
                "OFFICIAL_MCM", "https://example.com"
        );

        when(selectedProduct.getId()).thenReturn(404L);
        when(selectedProduct.getSku()).thenReturn("MWRFAXT01PZ001");
        when(selectedProduct.getProduct()).thenReturn(product);
        when(selectedProduct.getImageUrl()).thenReturn("products/tracy.png");
        when(selectedProduct.getPrice()).thenReturn(100_000L);
        when(selectedProduct.getDetailUrl()).thenReturn("https://example.com/tracy");
        when(product.getName()).thenReturn("상품 4");
        when(product.getDescription()).thenReturn("상품 4 설명");
        when(productVariantRepository.findById(404L)).thenReturn(Optional.of(selectedProduct));
        when(packingProfileCatalog.findBySku("MWRFAXT01PZ001"))
                .thenReturn(Optional.of(productFourProfile));

        ProductResponse response = productService.getProductByVariantId(404L);

        assertThat(response.getProductVariantId()).isEqualTo(404L);
        assertThat(response.getProductSku()).isEqualTo("MWRFAXT01PZ001");
        assertThat(response.getPackingProfileId()).isEqualTo("F04");
        assertThat(response.getPackingProfile().getPackingProfileId()).isEqualTo("F04");
    }
}
