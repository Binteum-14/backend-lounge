package com.lounge.domain.snack.dto.response;

import com.lounge.domain.snack.entity.Snack;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SnackResponse {

    private Long snackId;
    private String name;
    private String imageUrl;

    public static SnackResponse from(Snack snack) {
        return new SnackResponse(
                snack.getId(),
                snack.getName(),
                snack.getImageUrl()
        );
    }
}
