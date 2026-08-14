package com.lounge.domain.user.exception;

import com.lounge.global.api.code.BaseErrorCode;
import com.lounge.global.exception.GeneralException;

public class UserException extends GeneralException {

    public UserException(BaseErrorCode code) {
        super(code);
    }

    public static UserException of(BaseErrorCode code) {
        return new UserException(code);
    }
}
