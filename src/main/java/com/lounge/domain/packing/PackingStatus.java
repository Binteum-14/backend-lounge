package com.lounge.domain.packing;

public enum PackingStatus {

    /**
     * 여유롭게 수납 가능
     */
    COMFORTABLE,

    /**
     * 수납은 가능하지만 공간이 빠듯함
     */
    TIGHT,

    /**
     * 일부 물건이 들어가지 않거나 공간이 부족함
     */
    NOT_RECOMMENDED,

    /**
     * 제품 데이터에서 가방 크기를 찾지 못함
     */
    PROFILE_UNAVAILABLE
}