package com.lounge.domain.user.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMeResponse {

    private String username;

    public static UserMeResponse of(String username) {
        return new UserMeResponse(username);
    }
}
