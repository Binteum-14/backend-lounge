package com.lounge.domain.ownerchat.exception;

import com.lounge.global.api.code.BaseErrorCode;
import com.lounge.global.exception.GeneralException;

public class OwnerChatException extends GeneralException {

    private OwnerChatException(BaseErrorCode code) {
        super(code);
    }

    public static OwnerChatException of(BaseErrorCode code) {
        return new OwnerChatException(code);
    }
}
