package com.lounge.domain.auth.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UsernameCheckResponse {

    private Boolean available;

    public static UsernameCheckResponse of(boolean available) {
        return new UsernameCheckResponse(available);
    }
}
