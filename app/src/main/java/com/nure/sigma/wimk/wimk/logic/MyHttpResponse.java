package com.nure.sigma.wimk.wimk.logic;

import android.support.annotation.Nullable;


public class MyHttpResponse {
    public static final int OK = 0;
    public static final int BAD_URL = 101;
    public static final int OPEN_CONNECTION_FAIL = 102;
    public static final int NULL_CONNECTION = 103;
    public static final int SET_REQUEST_METHOD_FAIL = 104;
    public static final int OUTPUT_STREAM_FAIL = 105;
    public static final int GET_RESPONSE_FAIL = 106;

    public static final int SET_PAIRS_FAIL = 107;
    public static final int RESPONSE_NULL = 108;

    public static final int UNKNOWN_ERROR = 200;

    private int errorCode;
    private String response;

    public int getErrorCode() {
        return errorCode;
    }

    @Nullable
    public String getResponse() {
        return response;
    }

    public MyHttpResponse(int errorCode, String response) {
        this.errorCode = errorCode;
        this.response = response;
    }

    @Override
    public String toString() {
        return "errorCode==>" + getErrorCode() + " response==>" + getResponse();
    }
}
