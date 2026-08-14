package com.lounge.domain.diagnosisanswer.exception;

import com.lounge.global.api.code.BaseErrorCode;
import com.lounge.global.exception.GeneralException;

public class DiagnosisAnswerException extends GeneralException {

    public DiagnosisAnswerException(BaseErrorCode code) {
        super(code);
    }

    public static DiagnosisAnswerException of(BaseErrorCode code) {
        return new DiagnosisAnswerException(code);
    }
}
