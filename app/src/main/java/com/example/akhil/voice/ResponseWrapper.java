package com.example.akhil.voice;

/**
 * Created by akhil on 13/4/17.
 */

public class ResponseWrapper {

    private ResponseStatus responseStatus;
    private String responseString;
    private RequestType requestType;
    private String code;

    private StatusIdentifier deviceName, deviceStatus;

    public void setStatus(ResponseStatus responseStatus, String responseString,
                          RequestType requestType, String code) {

        this.responseStatus = responseStatus;
        this.responseString = responseString;
        this.requestType = requestType;
        this.code = code;
    }

    void setStatusIdenifier (StatusIdentifier deviceName, StatusIdentifier deviceStatus) {
        this.deviceName = deviceName;
        this.deviceStatus = deviceStatus;
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

    StatusIdentifier getDeviceIdentifier() {return deviceName;}

    StatusIdentifier getDeviceStatus() {return deviceStatus;}


}
