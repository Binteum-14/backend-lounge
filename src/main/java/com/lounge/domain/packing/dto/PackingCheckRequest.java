package com.lounge.domain.packing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PackingCheckRequest(

        @NotEmpty(message = "수납할 물건을 하나 이상 선택해주세요.")
        @Size(max = 12, message = "수납 물건은 최대 12개까지 선택할 수 있습니다.")
        List<
                @NotBlank(message = "물건 코드는 비어 있을 수 없습니다.")
                String
                > itemCodes
) {
}