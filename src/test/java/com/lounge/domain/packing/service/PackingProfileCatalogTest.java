package com.lounge.domain.packing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lounge.domain.packing.dto.PackingProfile;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class PackingProfileCatalogTest {

    private final PackingProfileCatalog catalog =
            new PackingProfileCatalog(new ObjectMapper());

    @Test
    void loadsAllTwentyOneMenuProfiles() {
        assertThat(catalog.findAll()).hasSize(21);
    }

    @Test
    void findsLoungeProfileByIdAndSkuWithoutCaseSensitivity() {
        PackingProfile profile = catalog.findByPackingProfileId("l01").orElseThrow();

        assertThat(profile.sku()).isEqualTo("MMKEAVE14CO001");
        assertThat(catalog.findBySku("mmkeave14co001"))
                .containsSame(profile);
        assertThat(profile.getImageUrl())
                .isEqualTo("/packing-assets/라운지/01_stark_side_studded_visetos_backpack_cognac.png");
    }

    @Test
    void findsFlightProfile() {
        PackingProfile profile = catalog.findByPackingProfileId("F02").orElseThrow();

        assertThat(profile.scene()).isEqualTo("flight");
        assertThat(profile.getImageUrl())
                .isEqualTo("/packing-assets/비행기/02_travia_quilted_shoulder_black.png");
    }

    @Test
    void findsPerfumeProfileWithItsTransparentBundledImage() {
        PackingProfile profile = catalog.findByPackingProfileId("P01").orElseThrow();

        assertThat(profile.scene()).isEqualTo("perfume");
        assertThat(profile.sku()).isEqualTo("MYZGATA05CO001");
        assertThat(profile.getImageUrl())
                .isEqualTo("/packing-assets/향수/01_mini_aren_triangle_crossbody_cognac.png");
    }

    @Test
    void everyProfileHasItsBundledBagImage() {
        assertThat(catalog.findAll())
                .allSatisfy(profile ->
                        assertThat(new ClassPathResource(
                                "static" + profile.getImageUrl()
                        ).exists()).isTrue()
                );
    }
}
