package com.developcollect.easycode.core.fakedata;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/14 16:01
 */
public class ReferenceNotFoundException extends RuntimeException {
    public ReferenceNotFoundException() {
    }

    public ReferenceNotFoundException(String message) {
        super(message);
    }

    public ReferenceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReferenceNotFoundException(Throwable cause) {
        super(cause);
    }

    public ReferenceNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
