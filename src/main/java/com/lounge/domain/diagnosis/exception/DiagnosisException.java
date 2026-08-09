package com.lounge.domain.diagnosis.exception;

import com.lounge.global.api.code.BaseErrorCode;
import com.lounge.global.exception.GeneralException;

public class DiagnosisException extends GeneralException {

    public DiagnosisException(BaseErrorCode code) {
        super(code);
    }

    public static DiagnosisException of(BaseErrorCode code) {
        return new DiagnosisException(code);
    }
}
