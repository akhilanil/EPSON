package com.example.akhil.epson;

/**
 * Created by akhil on 25/3/17.
 */

public class ParameterFactory {


    public static String getMode(RequestMode mode) {


        /*5 is reserved for ESP to ESP communication*/
        switch(mode) {

            case INIT:
                /*0 indicates init request*/
                return  "0";
            case RC4KEY:
                /*1 indicates RC4key*/
                return "1";
            case NORMAL:
                /*2 indicates Normal*/
                return "2";
            case LIGHT:
                /*3 indicates LIGHT*/
                return "3";
            case FINISH:
                /*9 indicates Finish*/
                return "9";
            default:
                return "ERROR";

        }
    }

    public static ServerResponse getServerResponse(int serverResponse) {

        switch (serverResponse) {

            case 0:
                /*WHEN HOST AT GIVEN IP ADDRESS AND PORT IS NOT AVAILABLE */
                return ServerResponse.UNREACHABLE_HOST;
            case 1:
                /*WHEN A REQUEST IS PROCESSED SUCCESSFULLY */
                return ServerResponse.SUCCESS;
            case 2:
                /*WHEN AUTHOURIZATION REQUEST IS PROCESSED SUCCESSFULLY */
                return ServerResponse.AUTH_SUCCESS;
            case 9:
                /*WHEN A REQUEST IS NOT PROCESSED SUCCESSFULLY */
                return ServerResponse.FAIL;
            case 8:
                /*WHEN AUTHOURIZATION REQUEST NOT PROCESSED SUCCESSFULLY */
                return ServerResponse.AUTH_FAIL;
            default:
                return  ServerResponse.UNREACHABLE_HOST;
        }
    }
}
