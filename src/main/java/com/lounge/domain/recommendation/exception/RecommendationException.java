package com.lounge.domain.recommendation.exception;

import com.lounge.global.api.code.BaseErrorCode;
import com.lounge.global.exception.GeneralException;

public class RecommendationException extends GeneralException {

    public RecommendationException(BaseErrorCode code) {
        super(code);
    }

    public static RecommendationException of(BaseErrorCode code) {
        return new RecommendationException(code);
    }
}
