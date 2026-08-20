package com.lounge.domain.packing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lounge.domain.packing.PackingStatus;
import com.lounge.domain.packing.dto.PackingCheckRequest;
import com.lounge.domain.packing.dto.PackingCheckResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class PackingServiceTest {

    private final PackingService packingService = new PackingService(
            new CarryItemCatalog(),
            new PackingProfileCatalog(new ObjectMapper()),
            new PackingLayoutEngine()
    );

    @Test
    void loungeBackpackAllowsSupportedLaptopAndTablet() {
        PackingCheckResponse response = packingService.check(
                "L01",
                new PackingCheckRequest(List.of("LAPTOP_13", "TABLET_11"))
        );

        assertThat(response.status()).isEqualTo(PackingStatus.COMFORTABLE);
        assertThat(response.items()).allMatch(PackingCheckResponse.ItemResult::fit);
        assertThat(response.placements()).hasSize(2);
        assertThat(response.bagSize().widthMm()).isEqualTo(300);
    }

    @Test
    void loungeMiniShopperRejectsLaptopByOfficialProfileSupport() {
        PackingCheckResponse response = packingService.check(
                "L02",
                new PackingCheckRequest(List.of("LAPTOP_13"))
        );

        assertThat(response.status()).isEqualTo(PackingStatus.NOT_RECOMMENDED);
        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.fit()).isFalse();
            assertThat(item.reason()).contains("노트북 수납");
        });
        assertThat(response.placements()).isEmpty();
    }

    @Test
    void flightProfileReturnsItsBagImageAndPackingResult() {
        PackingCheckResponse response = packingService.check(
                "F02",
                new PackingCheckRequest(List.of("SMARTPHONE", "CARD_WALLET"))
        );

        assertThat(response.scene()).isEqualTo("flight");
        assertThat(response.imageUrl())
                .isEqualTo("/packing-assets/비행기/02_travia_quilted_shoulder_black.png");
        assertThat(response.items()).allMatch(PackingCheckResponse.ItemResult::fit);
    }

    @Test
    void smartphoneFitsInEverySupportedPackingBag() {
        packingService.getAvailableProfiles().forEach(profile -> {
            PackingCheckResponse response = packingService.check(
                    profile.loungeId(),
                    new PackingCheckRequest(List.of("SMARTPHONE"))
            );

            assertThat(response.items())
                    .as(profile.loungeId() + " should fit a smartphone")
                    .allMatch(PackingCheckResponse.ItemResult::fit);
        });
    }

    @Test
    void smartphoneAndWalletFitInEverySupportedPackingBag() {
        packingService.getAvailableProfiles().forEach(profile -> {
            PackingCheckResponse response = packingService.check(
                        profile.loungeId(),
                        new PackingCheckRequest(List.of(
                            "SMARTPHONE", "CARD_WALLET"
                    ))
            );

            assertThat(response.items())
                    .as(profile.loungeId() + " should fit a phone and wallet")
                    .allMatch(PackingCheckResponse.ItemResult::fit);
        });
    }

    @Test
    void recommendedPackingUsesOnlyItemsThatFitTheBag() {
        PackingCheckResponse response = packingService.recommend("L01");

        assertThat(response.items()).hasSizeGreaterThanOrEqualTo(4);
        assertThat(response.items()).allMatch(PackingCheckResponse.ItemResult::fit);
        assertThat(response.usedSpaceRatio()).isGreaterThan(0.0);
        assertThat(response.usedSpaceRatio()).isLessThan(0.75);
    }

    @Test
    void selectedItemsChangeThePackingResultAndIncludeEverySelectedItem() {
        PackingCheckResponse lightPacking = packingService.check(
                "L01",
                new PackingCheckRequest(List.of("SMARTPHONE"))
        );
        PackingCheckResponse dailyPacking = packingService.check(
                "L01",
                new PackingCheckRequest(List.of(
                        "SMARTPHONE",
                        "CARD_WALLET",
                        "BOOK_PAPERBACK",
                        "SUNGLASSES_CASE",
                        "USB_C_CHARGER",
                        "CHARGING_CABLE"
                ))
        );

        assertThat(dailyPacking.usedSpaceRatio())
                .isGreaterThan(lightPacking.usedSpaceRatio());
        assertThat(dailyPacking.items())
                .extracting(PackingCheckResponse.ItemResult::itemCode)
                .containsExactlyInAnyOrder(
                        "SMARTPHONE",
                        "CARD_WALLET",
                        "BOOK_PAPERBACK",
                        "SUNGLASSES_CASE",
                        "USB_C_CHARGER",
                        "CHARGING_CABLE"
                );
        assertThat(dailyPacking.placements()).hasSize(6);
    }

    @Test
    void laptopAndBookKeepTheirPhysicalSizeRelationshipInTheXrayLayout() {
        PackingCheckResponse response = packingService.check(
                "L01",
                new PackingCheckRequest(List.of("LAPTOP_13", "BOOK_PAPERBACK"))
        );
        Map<String, PackingCheckResponse.Placement> placements = response.placements()
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        PackingCheckResponse.Placement::itemCode,
                        Function.identity()
                ));

        PackingCheckResponse.Placement laptop = placements.get("LAPTOP_13");
        PackingCheckResponse.Placement book = placements.get("BOOK_PAPERBACK");

        assertThat(laptop.width()).isGreaterThan(book.width());
        assertThat(laptop.height()).isLessThan(book.height());
    }

    @Test
    void loungeMiniShopperAllowsDailyPouchByRotatingItIntoTheBag() {
        PackingCheckResponse response = packingService.check(
                "L02",
                new PackingCheckRequest(List.of("POUCH"))
        );

        assertThat(response.status()).isEqualTo(PackingStatus.COMFORTABLE);
        assertThat(response.usedSpaceRatio()).isLessThanOrEqualTo(1.0);
        assertThat(response.items())
                .filteredOn(item -> item.itemCode().equals("POUCH"))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.fit()).isTrue();
                });
        assertThat(response.placements())
                .extracting(PackingCheckResponse.Placement::itemCode)
                .contains("POUCH");
    }
}
