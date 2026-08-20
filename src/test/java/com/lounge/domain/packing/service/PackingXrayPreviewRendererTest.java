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
    void rendersEveryLoungeAndFlightBagWithItsOwnImage() {
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
                    new PackingCheckRequest(List.of("SMARTPHONE"))
            );
            String svg = renderer.render(response);

            assertThat(svg)
                    .contains("<svg")
                    .contains(response.productName())
                    .contains("data:image/png;base64,");
        }
    }
}
