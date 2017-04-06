package com.example.akhil.epson;

/**
 * Created by akhil on 6/4/17.
 */

public class ResponseVoice {


    public void decodeResponse() {

    }

    public static String  getResponse(String query) {

        String response = "sorry, couldn't get that";

        if(query.equals("lights on") || query.equals("light on"))
            response = "affirmative, turning lights on ";
        else if(query.equals("lights off") || query.equals("light off"))
            response = "affirmative, turning lights off ";
        else if(query.equals("projector on"))
            response ="affirmative, turning projector on ";
        else if(query.equals("projector off"))
            response ="affirmative, turning projector off ";

        return response;


    }



}
