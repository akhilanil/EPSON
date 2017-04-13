package com.example.akhil.voice;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by akhil on 14/4/17.
 */

public class PatternRecognition {

    public String voiceCommand;

    private ArrayList<String> patternDevice, patternStatus;

    public ResponseWrapper responseWrapper;
    private boolean devicematch,statusmatch;
    private StatusIdentifier deviceIdentifier, deviceStatus;

    private final int TOTAL_PATTERNS = 2;
    private final int TOTAL_STATUS = 2;

    public PatternRecognition(String voiceCommand) {

        this.voiceCommand = voiceCommand;

        devicematch = false;
        statusmatch = false;

        deviceIdentifier = StatusIdentifier.NONE;
        deviceStatus = StatusIdentifier.NONE;

        this.responseWrapper = new ResponseWrapper();

        setDefaultCommands();
    }

    public ResponseWrapper decodeCommand() {

        int i = 0;

        while(i < TOTAL_PATTERNS && !devicematch) {

            Pattern pattern = Pattern.compile(patternDevice.get(i));
            Matcher matcher = pattern.matcher(voiceCommand);

            if(matcher.find()){
                deviceIdentifier = getDeviceIdentifier(i);
                devicematch = true;
            }
            i++;
        }

        if(devicematch) {
            i = 0;
            while(i < TOTAL_STATUS && !statusmatch) {

                Pattern pattern = Pattern.compile(patternStatus.get(i));
                Matcher matcher = pattern.matcher(voiceCommand);

                if(matcher.find()){
                    deviceStatus = getDeviceStatus(i);
                    statusmatch = true;
                }
                i++;
            }
        }

        if(!devicematch || !statusmatch) {
            responseWrapper.setStatusIdenifier(deviceIdentifier, deviceStatus);
        }
        else {
            responseWrapper.setStatusIdenifier(deviceIdentifier, deviceStatus);
        }
        return responseWrapper;
    }

    private void setDefaultCommands() {

        patternDevice = new ArrayList<String>();
        patternDevice.add(0, "light|lights");
        patternDevice.add(1, "projector");

        patternStatus = new ArrayList<String>();
        patternStatus.add(0, "ON");
        patternStatus.add(1, "OFF");
    }

    private StatusIdentifier getDeviceIdentifier(int searchValue) {

        switch (searchValue) {

            case 0:
                return StatusIdentifier.LIGHT;
            case 1:
                return StatusIdentifier.PRJECTOR;
            default:
                return StatusIdentifier.NONE;
        }
    }

    private StatusIdentifier getDeviceStatus(int searchValue) {

        switch (searchValue) {

            case 0:
                return StatusIdentifier.ON;
            case 1:
                return StatusIdentifier.OFF;
            default:
                return StatusIdentifier.NONE;
        }
    }


}
