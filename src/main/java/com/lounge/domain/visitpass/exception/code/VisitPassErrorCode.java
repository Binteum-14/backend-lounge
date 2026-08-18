package com.lounge.domain.visitpass.exception.code;

import com.lounge.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum VisitPassErrorCode implements BaseErrorCode {

    VISIT_PASS_NOT_FOUND(HttpStatus.NOT_FOUND, "VISIT_PASS_404_1", "방문 패스를 찾을 수 없습니다."),
    VISIT_PASS_FORBIDDEN(HttpStatus.FORBIDDEN, "VISIT_PASS_403_1", "해당 추천 결과에 대한 방문 패스를 발급할 수 없습니다."),
    VISIT_PASS_QR_GENERATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "VISIT_PASS_500_1", "QR 이미지 생성에 실패했습니다."),
    VISIT_PASS_QR_UPLOAD_FAILED(HttpStatus.BAD_GATEWAY, "VISIT_PASS_502_1", "QR 이미지 업로드에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
