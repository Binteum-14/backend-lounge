package com.lounge.domain.flightfocusdetail.exception;

import com.lounge.global.api.code.BaseErrorCode;
import com.lounge.global.exception.GeneralException;

public class FlightFocusDetailException extends GeneralException {

    public FlightFocusDetailException(BaseErrorCode code) {
        super(code);
    }

    public static FlightFocusDetailException of(BaseErrorCode code) {
        return new FlightFocusDetailException(code);
    }
}
