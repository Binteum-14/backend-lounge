package com.lounge.domain.recommendationproduct.exception;

import com.lounge.global.api.code.BaseErrorCode;
import com.lounge.global.exception.GeneralException;

public class RecommendationProductException extends GeneralException {

    public RecommendationProductException(BaseErrorCode code) {
        super(code);
    }

    public static RecommendationProductException of(BaseErrorCode code) {
        return new RecommendationProductException(code);
    }
}
