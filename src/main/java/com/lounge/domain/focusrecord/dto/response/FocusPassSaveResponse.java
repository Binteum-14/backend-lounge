package com.lounge.domain.focusrecord.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FocusPassSaveResponse {

    private Long focusRecordId;

    public static FocusPassSaveResponse of(Long focusRecordId) {
        return new FocusPassSaveResponse(focusRecordId);
    }
}
