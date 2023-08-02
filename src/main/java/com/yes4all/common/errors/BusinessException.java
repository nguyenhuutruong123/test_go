package com.yes4all.common.errors;

public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 3602587771075091036L;

    protected String errorCode;
    protected String errorDesc;

    public BusinessException(String errorDesc) {
        super(errorDesc);
        this.errorDesc = errorDesc;
    }

    public BusinessException(String errorCode, String errorDesc) {
        super(errorDesc);
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }
}
