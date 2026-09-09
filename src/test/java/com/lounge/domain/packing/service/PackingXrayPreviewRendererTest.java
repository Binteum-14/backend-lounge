package com.lounge.domain.packing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lounge.domain.packing.dto.PackingCheckRequest;
import com.lounge.domain.packing.dto.PackingCheckResponse;
import com.lounge.domain.packing.dto.PackingProfile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PackingXrayPreviewRendererTest {

    @Test
    void rendersSelectedItemsIntoAnSvgImage() {
        PackingService packingService = new PackingService(
                new CarryItemCatalog(),
                new PackingProfileCatalog(new ObjectMapper()),
                new PackingLayoutEngine()
        );
        PackingCheckResponse response = packingService.check(
                "L01",
                new PackingCheckRequest(List.of("LAPTOP_13", "TABLET_11"))
        );

        String svg = new PackingXrayPreviewRenderer().render(response);

        assertThat(svg).contains("<svg");
        assertThat(svg).contains("AI PACKING");
        assertThat(svg).contains("data:image/png;base64,");
        assertThat(svg).contains("13인치 노트북");
        assertThat(svg).contains("11인치 태블릿");
    }

    @Test
    void keepsALaptopAsTheCenteredVisualAnchor() {
        PackingService packingService = new PackingService(
                new CarryItemCatalog(),
                new PackingProfileCatalog(new ObjectMapper()),
                new PackingLayoutEngine()
        );
        PackingCheckResponse response = packingService.check(
                "L01",
                new PackingCheckRequest(List.of(
                        "TABLET_11", "LAPTOP_13", "SMARTPHONE"
                ))
        );

        String svg = new PackingXrayPreviewRenderer().render(response);

        assertThat(svg).contains("data-item=\"LAPTOP_13\" data-primary=\"true\"");
        assertThat(svg).contains("data-item=\"TABLET_11\" data-primary=\"false\"");
        assertThat(svg).contains("data-item=\"SMARTPHONE\" data-primary=\"false\"");
    }

    @Test
    void rendersALaptopWithItsOwnDominantXrayScale() {
        PackingService packingService = new PackingService(
                new CarryItemCatalog(),
                new PackingProfileCatalog(new ObjectMapper()),
                new PackingLayoutEngine()
        );
        PackingCheckResponse response = packingService.check(
                "L01",
                new PackingCheckRequest(List.of("LAPTOP_13", "TABLET_11"))
        );

        String svg = new PackingXrayPreviewRenderer().render(response);

        // A 13-inch laptop uses the rear sleeve and must not look like a
        // second tablet; its raster gets the dedicated 1.20 optical scale.
        assertThat(svg).contains("data-item=\"LAPTOP_13\" data-primary=\"true\"");
        assertThat(svg).contains("width=\"376.75\"");
    }

    @Test
    void usesTheActualItemToBagScaleInsteadOfDrawingMiniatures() {
        PackingService packingService = new PackingService(
                new CarryItemCatalog(),
                new PackingProfileCatalog(new ObjectMapper()),
                new PackingLayoutEngine()
        );
        PackingCheckResponse response = packingService.check(
                "L01",
                new PackingCheckRequest(List.of("TABLET_11"))
        );

        String svg = new PackingXrayPreviewRenderer().render(response);

        // An 11-inch tablet is 250 mm wide in a 300 mm backpack. Its rendered
        // bitmap keeps a 229.49 px landscape footprint after rotation,
        // rather than the old miniature (~150 px) caused by double scaling.
        assertThat(svg).contains("data-item=\"TABLET_11\" data-primary=\"true\"");
        assertThat(svg).contains("height=\"229.49\"");
    }

    @Test
    void keepsPackedItemsInsideTheScannerWithoutCroppingThemToTheBagPhotoSilhouette() {
        PackingService packingService = new PackingService(
                new CarryItemCatalog(),
                new PackingProfileCatalog(new ObjectMapper()),
                new PackingLayoutEngine()
        );
        PackingCheckResponse response = packingService.check(
                "L01",
                new PackingCheckRequest(List.of("LAPTOP_13", "CARD_WALLET"))
        );

        String svg = new PackingXrayPreviewRenderer().render(response);

        assertThat(svg).contains("data-packing-contents=\"true\" clip-path=\"url(#bagClip)\"");
    }

    @Test
    void showsAnExplicitEmptyStateWhenNoSelectedItemFits() {
        PackingService packingService = new PackingService(
                new CarryItemCatalog(),
                new PackingProfileCatalog(new ObjectMapper()),
                new PackingLayoutEngine()
        );
        PackingCheckResponse response = packingService.check(
                "L02",
                new PackingCheckRequest(List.of("LAPTOP_13"))
        );

        String svg = new PackingXrayPreviewRenderer().render(response);

        assertThat(svg).contains("NO PACKABLE ITEMS");
        assertThat(svg).doesNotContain("data-item=\"LAPTOP_13\"");
    }

    @Test
    void rendersEveryMenuBagWithItsOwnImageAndEverydayItemsInsideTheScanner() {
        PackingProfileCatalog catalog = new PackingProfileCatalog(new ObjectMapper());
        PackingService packingService = new PackingService(
                new CarryItemCatalog(),
                catalog,
                new PackingLayoutEngine()
        );
        PackingXrayPreviewRenderer renderer = new PackingXrayPreviewRenderer();

        for (PackingProfile profile : catalog.findAll()) {
            PackingCheckResponse response = packingService.check(
                    profile.loungeId(),
                    new PackingCheckRequest(List.of(
                            "SMARTPHONE", "POUCH", "CARD_WALLET"
                    ))
            );
            String svg = renderer.render(response);

            assertThat(svg)
                    .contains("<svg")
                    .contains(response.productName())
                    .contains("data:image/png;base64,")
                    .contains("data-packing-contents=\"true\" clip-path=\"url(#bagClip)\"");
            response.items().stream()
                    .filter(PackingCheckResponse.ItemResult::fit)
                    .forEach(item -> assertThat(svg).contains("data-item=\"" + item.itemCode() + "\""));
        }
    }

    @Test
    void namesTheItemThatMustBeExcludedInsteadOfCallingTheWholeBagUnusable() {
        PackingService packingService = new PackingService(
                new CarryItemCatalog(),
                new PackingProfileCatalog(new ObjectMapper()),
                new PackingLayoutEngine()
        );
        PackingCheckResponse response = packingService.check(
                "L02",
                new PackingCheckRequest(List.of(
                        "SMARTPHONE", "CARD_WALLET", "BOOK_PAPERBACK"
                ))
        );

        String svg = new PackingXrayPreviewRenderer().render(response);

        assertThat(svg).contains("일부 물품 제외 필요");
        assertThat(svg).contains("책 제외 필요");
    }
}
