package com.lounge.domain.snack.service;

import com.lounge.domain.product.entity.ProductVariant;
import com.lounge.domain.snack.dto.response.SnackDetailResponse;
import com.lounge.domain.snack.entity.Snack;
import com.lounge.domain.snack.entity.SnackSet;
import com.lounge.domain.snack.entity.SnackType;
import com.lounge.domain.snack.repository.SnackRepository;
import com.lounge.domain.snack.repository.SnackSetRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnackServiceTest {

    @Mock
    private SnackRepository snackRepository;

    @Mock
    private SnackSetRepository snackSetRepository;

    @InjectMocks
    private SnackService snackService;

    @ParameterizedTest
    @EnumSource(SnackType.class)
    void mapsEveryMenuCategoryToItsOwnPackingBag(
            SnackType snackType
    ) {
        Snack first = snack(1L);
        Snack second = snack(2L);
        Snack third = snack(3L);
        Snack fourth = snack(4L);
        Snack fifth = snack(5L);
        Snack sixth = snack(6L);
        Snack seventh = snack(7L);
        List<Snack> menu = List.of(
                first, second, third, fourth, fifth, sixth, seventh
        );
        when(fourth.getType()).thenReturn(snackType);

        ProductVariant productVariant = mock(ProductVariant.class);
        when(productVariant.getId()).thenReturn(91L);
        when(productVariant.getSku()).thenReturn("ANY-SKU");

        SnackSet snackSet = mock(SnackSet.class);
        when(snackSet.getProductVariant()).thenReturn(productVariant);

        when(snackRepository.findByIdAndActiveTrue(4L))
                .thenReturn(Optional.of(fourth));
        when(snackSetRepository.findBySnack_Id(4L))
                .thenReturn(Optional.of(snackSet));
        when(snackRepository.findByTypeAndActiveTrueOrderByIdAsc(snackType))
                .thenReturn(menu);

        SnackDetailResponse response = snackService.getSnack(4L);

        String expectedPackingProfileId = switch (snackType) {
            case SNACK -> "L04";
            case DRINK -> "F04";
            case PERFUME -> "P04";
        };
        assertThat(response.getPackingProfileId())
                .isEqualTo(expectedPackingProfileId);
        assertThat(response.getLoungePackingProfileId()).isEqualTo("L04");
        assertThat(response.getFlightPackingProfileId()).isEqualTo("F04");
    }

    private Snack snack(Long id) {
        Snack snack = mock(Snack.class);
        when(snack.getId()).thenReturn(id);
        return snack;
    }
}
