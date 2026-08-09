package com.lounge.domain.visitpass.exception;

import com.lounge.global.api.code.BaseErrorCode;
import com.lounge.global.exception.GeneralException;

public class VisitPassException extends GeneralException {

    public VisitPassException(BaseErrorCode code) {
        super(code);
    }

    public static VisitPassException of(BaseErrorCode code) {
        return new VisitPassException(code);
    }
}
