package com.lounge.domain.presence.exception.code;

import com.lounge.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PresenceSuccessCode implements BaseSuccessCode {

    PRESENCE_HEARTBEAT_SUCCESS(
            HttpStatus.OK,
            "PRESENCE_200_1",
            "현재 이용 인원 갱신에 성공했습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
