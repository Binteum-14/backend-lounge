package com.lounge.domain.focusrecord.exception;

import com.lounge.global.api.code.BaseErrorCode;
import com.lounge.global.exception.GeneralException;

public class FocusRecordException extends GeneralException {

    public FocusRecordException(BaseErrorCode code) {
        super(code);
    }

    public static FocusRecordException of(BaseErrorCode code) {
        return new FocusRecordException(code);
    }
}
