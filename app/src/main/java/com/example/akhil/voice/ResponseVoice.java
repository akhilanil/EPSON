package com.example.akhil.voice;

import android.app.DownloadManager;

/**
 * Created by akhil on 13/4/17.
 */

public class ResponseVoice{


    private static boolean lightStatus = false;
    private static boolean projectorStatus = false;



    public static ResponseWrapper getResponse(String query) {

        ResponseWrapper responseWrapper;

        PatternRecognition patternRecognition = new PatternRecognition(query);
        responseWrapper = patternRecognition.decodeCommand();


        RequestType requestType = null;
        ResponseStatus responseStatus = ResponseStatus.RESPONSE_FAIL;
        String response = "sorry, couldn't get that";
        String code = "";


        if(responseWrapper.getDeviceIdentifier().equals(StatusIdentifier.NONE)) {

        }
        else if(responseWrapper.getDeviceIdentifier().equals(StatusIdentifier.LIGHT)) {

            if(responseWrapper.getDeviceStatus().equals(StatusIdentifier.ON)) {

                if (!lightStatus) {
                    response = "affirmative, turning lights on ";
                    responseStatus = ResponseStatus.RESPONSE_SUCCESS;
                    requestType = RequestType.LIGHT;
                    code = "";
                    changeLightSatus();
                } else {
                    response = "Lights Already On";
                    responseStatus = ResponseStatus.RESPONSE_FAIL;

                }
            }
            else if(responseWrapper.getDeviceStatus().equals(StatusIdentifier.OFF)) {
                if (lightStatus) {
                    response = "affirmative, turning lights off ";
                    responseStatus = ResponseStatus.RESPONSE_SUCCESS;
                    requestType = RequestType.LIGHT;
                    code = "";
                    changeLightSatus();
                }
                else{
                    response = "Lights already off";
                    responseStatus = ResponseStatus.RESPONSE_FAIL;
                }
            }
        }

        else if(responseWrapper.getDeviceIdentifier().equals(StatusIdentifier.PRJECTOR)) {

            if(responseWrapper.getDeviceStatus().equals(StatusIdentifier.ON)) {
                if (!projectorStatus) {
                    response = "affirmative, turning projector on ";
                    responseStatus = ResponseStatus.RESPONSE_SUCCESS;
                    requestType = RequestType.PROJECTOR_STATE;
                    code = "powerButton";
                    changeProjectorStatus();
                } else {
                    response = "Projector Already On";
                    responseStatus = ResponseStatus.RESPONSE_FAIL;
                }
            }
            else if(responseWrapper.getDeviceStatus().equals(StatusIdentifier.OFF)) {
                if(projectorStatus) {
                    response = "affirmative, turning projector off ";
                    responseStatus = ResponseStatus.RESPONSE_SUCCESS;
                    requestType = RequestType.PROJECTOR_STATE;
                    code = "powerButton";
                    changeProjectorStatus();
                }
                else {
                    response = "Projector Already Off";
                    responseStatus = ResponseStatus.RESPONSE_FAIL;
                }
            }

        }

        responseWrapper.setStatus(responseStatus, response, requestType, code);
        return responseWrapper;
    }

    public static void changeLightSatus() {
        lightStatus = !lightStatus;
    }

    public static void changeProjectorStatus() { projectorStatus = !projectorStatus; }
}
