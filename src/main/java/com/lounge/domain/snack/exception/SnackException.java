package com.lounge.domain.snack.exception;

import com.lounge.global.api.code.BaseErrorCode;
import com.lounge.global.exception.GeneralException;

public class SnackException extends GeneralException {

    public SnackException(BaseErrorCode code) {
        super(code);
    }

    public static SnackException of(BaseErrorCode code) {
        return new SnackException(code);
    }
}
