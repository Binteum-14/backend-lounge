package com.lounge.domain.focusrecord.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FocusPassListResponse {

    private List<FocusPassResponse> items;
    private Long nextCursorId;
    private Boolean hasNext;

    public static FocusPassListResponse of(
            List<FocusPassResponse> items,
            Long nextCursorId,
            Boolean hasNext
    ) {
        return new FocusPassListResponse(items, nextCursorId, hasNext);
    }
}
