package com.lounge.domain.packing.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lounge.domain.packing.dto.PackingProfile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Loads the curated MCM bag profiles bundled with the application.
 */
@Component
public class PackingProfileCatalog {

    private static final String RESOURCE_PATH =
            "packing/mcm-packing-profiles.json";

    private final List<PackingProfile> profiles;

    private final Map<String, PackingProfile> profilesByLoungeId;

    private final Map<String, PackingProfile> profilesBySku;

    public PackingProfileCatalog(ObjectMapper objectMapper) {
        this.profiles = loadProfiles(objectMapper);
        this.profilesByLoungeId = indexByLoungeId(profiles);
        this.profilesBySku = indexBySku(profiles);
    }

    public List<PackingProfile> findAll() {
        return profiles;
    }

    public Optional<PackingProfile> findByLoungeId(String loungeId) {
        return findByKey(profilesByLoungeId, loungeId);
    }

    public Optional<PackingProfile> findBySku(String sku) {
        return findByKey(profilesBySku, sku);
    }

    private List<PackingProfile> loadProfiles(ObjectMapper objectMapper) {
        ClassPathResource resource = new ClassPathResource(RESOURCE_PATH);

        try (InputStream inputStream = resource.getInputStream()) {
            List<PackingProfile> loadedProfiles = objectMapper.readValue(
                    inputStream,
                    new TypeReference<>() {
                    }
            );

            if (loadedProfiles == null || loadedProfiles.isEmpty()) {
                throw new IllegalStateException(
                        "수납 프로필 파일이 비어 있습니다: " + RESOURCE_PATH
                );
            }

            return List.copyOf(loadedProfiles);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "수납 프로필 파일을 읽을 수 없습니다: " + RESOURCE_PATH,
                    exception
            );
        }
    }

    private Map<String, PackingProfile> indexByLoungeId(
            List<PackingProfile> profiles
    ) {
        Map<String, PackingProfile> index = new LinkedHashMap<>();

        for (PackingProfile profile : profiles) {
            validateProfile(profile);
            putUnique(index, profile.loungeId(), profile, "loungeId");
        }

        return Map.copyOf(index);
    }

    private Map<String, PackingProfile> indexBySku(
            List<PackingProfile> profiles
    ) {
        Map<String, PackingProfile> index = new LinkedHashMap<>();

        for (PackingProfile profile : profiles) {
            putUnique(index, profile.sku(), profile, "sku");
        }

        return Map.copyOf(index);
    }

    private void validateProfile(PackingProfile profile) {
        if (profile == null
                || isBlank(profile.loungeId())
                || isBlank(profile.sku())
                || isBlank(profile.productName())
                || profile.widthCm() == null || profile.widthCm() <= 0
                || profile.heightCm() == null || profile.heightCm() <= 0
                || profile.depthCm() == null || profile.depthCm() <= 0) {
            throw new IllegalStateException(
                    "유효하지 않은 수납 프로필이 있습니다: " + profile
            );
        }
    }

    private void putUnique(
            Map<String, PackingProfile> index,
            String key,
            PackingProfile profile,
            String keyName
    ) {
        String normalizedKey = normalize(key);

        if (index.putIfAbsent(normalizedKey, profile) != null) {
            throw new IllegalStateException(
                    "중복된 수납 프로필 " + keyName + "입니다: " + key
            );
        }
    }

    private Optional<PackingProfile> findByKey(
            Map<String, PackingProfile> index,
            String key
    ) {
        if (isBlank(key)) {
            return Optional.empty();
        }

        return Optional.ofNullable(index.get(normalize(key)));
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
