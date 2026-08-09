package com.lounge.domain.product.exception;

import com.lounge.global.api.code.BaseErrorCode;
import com.lounge.global.exception.GeneralException;

public class ProductException extends GeneralException {

    public ProductException(BaseErrorCode code) {
        super(code);
    }

    public static ProductException of(BaseErrorCode code) {
        return new ProductException(code);
    }
}
