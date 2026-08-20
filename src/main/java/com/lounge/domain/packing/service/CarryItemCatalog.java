package com.lounge.domain.packing.service;

import com.lounge.domain.packing.dto.PackingItemDefinition;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CarryItemCatalog {

    private final Map<String, PackingItemDefinition> items =
            new LinkedHashMap<>();

    public CarryItemCatalog() {

        register(new PackingItemDefinition(
                "SMARTPHONE",
                "스마트폰",
                "DIGITAL",
                78,
                162,
                9,
                "일반 대형 스마트폰 기준"
        ));

        register(new PackingItemDefinition(
                "CARD_WALLET",
                "반지갑",
                "DAILY",
                110,
                90,
                25,
                "일반 반지갑 기준"
        ));

        register(new PackingItemDefinition(
                "POUCH",
                "파우치",
                "DAILY",
                190,
                120,
                60,
                "일반 화장품 파우치 기준"
        ));

        register(new PackingItemDefinition(
                "TABLET_11",
                "11인치 태블릿",
                "DIGITAL",
                250,
                180,
                7,
                "11인치급 태블릿 기준"
        ));

        register(new PackingItemDefinition(
                "LAPTOP_13",
                "13인치 노트북",
                "DIGITAL",
                304,
                215,
                16,
                "13인치급 노트북 기준"
        ));

        register(new PackingItemDefinition(
                "LAPTOP_15",
                "15인치 노트북",
                "DIGITAL",
                360,
                245,
                18,
                "15인치급 노트북 기준"
        ));

        register(new PackingItemDefinition(
                "TUMBLER",
                "텀블러",
                "DAILY",
                75,
                210,
                75,
                "약 500ml 텀블러 기준"
        ));

        register(new PackingItemDefinition(
                "CAMERA",
                "카메라",
                "TRAVEL",
                130,
                95,
                75,
                "미러리스 카메라 바디 기준"
        ));

        register(new PackingItemDefinition(
                "FOLDING_UMBRELLA",
                "접이식 우산",
                "TRAVEL",
                55,
                270,
                55,
                "일반 접이식 우산 기준"
        ));

        register(new PackingItemDefinition(
                "NOTEBOOK_A5",
                "A5 노트",
                "STUDY",
                148,
                210,
                18,
                "A5 노트 기준"
        ));

        register(new PackingItemDefinition(
                "HEADPHONES_CASE",
                "헤드폰 케이스",
                "DIGITAL",
                190,
                160,
                85,
                "오버이어 헤드폰 케이스 기준"
        ));

        register(new PackingItemDefinition(
                "POWER_BANK",
                "보조배터리",
                "DIGITAL",
                150,
                70,
                20,
                "일반 휴대용 보조배터리 기준"
        ));
    }

    private void register(PackingItemDefinition item) {
        items.put(item.code(), item);
    }

    public Optional<PackingItemDefinition> findByCode(String code) {

        if (code == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                items.get(code.trim().toUpperCase())
        );
    }

    public List<PackingItemDefinition> findAll() {
        return List.copyOf(items.values());
    }
}