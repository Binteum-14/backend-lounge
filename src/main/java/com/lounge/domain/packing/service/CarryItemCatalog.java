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
                "지갑",
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

        register(new PackingItemDefinition(
                "BOOK_PAPERBACK",
                "책",
                "DAILY",
                150,
                225,
                28,
                "일반 단행본 기준"
        ));

        register(new PackingItemDefinition(
                "SUNGLASSES_CASE",
                "선글라스 케이스",
                "DAILY",
                165,
                70,
                50,
                "일반 선글라스 케이스 기준"
        ));

        register(new PackingItemDefinition(
                "USB_C_CHARGER",
                "충전기",
                "DIGITAL",
                65,
                65,
                32,
                "USB-C 충전 어댑터 기준"
        ));

        register(new PackingItemDefinition(
                "CHARGING_CABLE",
                "충전 케이블",
                "DIGITAL",
                100,
                80,
                28,
                "케이블을 정리한 상태 기준"
        ));

        register(new PackingItemDefinition(
                "EARBUDS_CASE",
                "무선 이어폰",
                "DIGITAL",
                65,
                55,
                30,
                "무선 이어폰 충전 케이스 기준"
        ));

        register(new PackingItemDefinition(
                "PASSPORT",
                "여권",
                "TRAVEL",
                125,
                88,
                12,
                "대한민국 여권 크기 기준"
        ));

        register(new PackingItemDefinition(
                "KEY_CASE",
                "키 케이스",
                "DAILY",
                110,
                70,
                30,
                "차 키를 포함한 키 케이스 기준"
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
