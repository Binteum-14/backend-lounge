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
    void loadsAllFourteenSceneProfiles() {
        assertThat(catalog.findAll()).hasSize(14);
    }

    @Test
    void findsProfileByLoungeIdAndSkuWithoutCaseSensitivity() {
        PackingProfile profile = catalog.findByLoungeId("l01").orElseThrow();

        assertThat(profile.sku()).isEqualTo("MMKEAVE14CO001");
        assertThat(catalog.findBySku("mmkeave14co001"))
                .containsSame(profile);
        assertThat(profile.getImageUrl())
                .isEqualTo("/packing-assets/라운지/01_stark_side_studded_visetos_backpack_cognac.png");
    }

    @Test
    void findsFlightProfile() {
        PackingProfile profile = catalog.findByLoungeId("F02").orElseThrow();

        assertThat(profile.scene()).isEqualTo("flight");
        assertThat(profile.getImageUrl())
                .isEqualTo("/packing-assets/비행기/02_travia_quilted_shoulder_black.png");
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
