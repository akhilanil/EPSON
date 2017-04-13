package com.example.akhil.voice;

/**
 * Created by akhil on 13/4/17.
 */

public class ResponseWrapper {

    private ResponseStatus responseStatus;
    private String responseString;
    private RequestType requestType;
    private String code;

    public void setStatus(ResponseStatus responseStatus, String responseString,
                          RequestType requestType, String code) {

        this.responseStatus = responseStatus;
        this.responseString = responseString;
        this.requestType = requestType;
        this.code = code;
    }

    public ResponseStatus getResponseStatus() {
        return this.responseStatus;
    }

    public String getResponseString() {
        return responseString;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public String getCode() {
        return code;
    }


}
