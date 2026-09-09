package com.lounge.domain.packing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lounge.domain.packing.dto.PackingProfile;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PackingProfileCatalogTest {

    private final PackingProfileCatalog catalog =
            new PackingProfileCatalog(new ObjectMapper());

    @Test
    void loadsAllTwentyOneMenuProfiles() {
        assertThat(catalog.findAll()).hasSize(21);
    }

    @Test
    void resolvesEverySupportedProductSkuToItsOwnPackingProfile() {
        Map<String, String> expectedProfileIdBySku = Map.ofEntries(
                Map.entry("MMKEAVE12CO001", "L01"),
                Map.entry("MWPAATN04BK001", "L02"),
                Map.entry("MWPFSLR03K8001", "L03"),
                Map.entry("MMVGATT01PZ001", "L04"),
                Map.entry("MWSGATA01I8001", "L05"),
                Map.entry("MMKEAVE12WT001", "L06"),
                Map.entry("MWPGSMT03WT001", "L07"),
                Map.entry("MWSEAAK01CK001", "F01"),
                Map.entry("MWSCSLM02BK001", "F02"),
                Map.entry("MWHFATA02I8001", "F03"),
                Map.entry("MWRFAXT01PZ001", "F04"),
                Map.entry("MWSESAC05DG001", "F05"),
                Map.entry("MWSEAAK04WT001", "F06"),
                Map.entry("MWRGSTA02CO001", "F07"),
                Map.entry("MYZGATA05CO001", "P01"),
                Map.entry("MMKEAVE05CO001", "P02"),
                Map.entry("MYZGATA01BK001", "P03"),
                Map.entry("MWKGATA03PZ001", "P04"),
                Map.entry("MWDESAC03DG001", "P05"),
                Map.entry("MWSFSAK01BK001", "P06"),
                Map.entry("MMTGSTA01CO001", "P07")
        );

        assertThat(expectedProfileIdBySku).hasSize(21);
        expectedProfileIdBySku.forEach((sku, packingProfileId) -> {
            PackingProfile profile = catalog.findBySku(sku).orElseThrow();

            assertThat(profile.getPackingProfileId()).isEqualTo(packingProfileId);
            assertThat(catalog.findByPackingProfileId(packingProfileId))
                    .containsSame(profile);
        });
    }

    @Test
    void findsLoungeProfileByIdAndSkuWithoutCaseSensitivity() {
        PackingProfile profile = catalog.findByPackingProfileId("l01").orElseThrow();

        assertThat(profile.sku()).isEqualTo("MMKEAVE12CO001");
        assertThat(catalog.findBySku("mmkeave12co001"))
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
