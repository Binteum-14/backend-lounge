package com.lounge.domain.visitpassproduct.exception;

import com.lounge.global.api.code.BaseErrorCode;
import com.lounge.global.exception.GeneralException;

public class VisitPassProductException extends GeneralException {

    public VisitPassProductException(BaseErrorCode code) {
        super(code);
    }

    public static VisitPassProductException of(BaseErrorCode code) {
        return new VisitPassProductException(code);
    }
}
