package com.example.akhil.decode;

import android.util.Log;

import com.example.akhil.epson.DeviceList;

/**
 * Created by akhil on 11/3/17.
 */

public class EncryptCode {

    RemoteCode remoteCode;
    EncryptionKeys encryptionKeys;
    RC4Encryption rc4Encryption;
    InitRC4 initRC4;

    public EncryptCode(InitRC4 initRC4, RemoteCode remoteCode) {
        Log.d("Constructor","ENCRYPTCODE");
        this.initRC4 = initRC4;
        this.remoteCode = remoteCode;
        rc4Encryption = new RC4Encryption(this.initRC4);
    }

    public String getCode(String buttonPressed, DeviceList deviceList) {
        String encryptedCode = "";
        String buttonCode = "";

        if(deviceList.equals(DeviceList.PROJECTOR)) {
            buttonCode = remoteCode.getRemoteCode(buttonPressed);
        }
        else if (deviceList.equals(DeviceList.LIGHT)){ /*NO IR  CODE NEEDED TO SWITCH ON THE LIGHT*/
            buttonCode = buttonPressed;
        }

        Log.d("CODE",buttonCode);

        if(!buttonCode.equals("XX"))
            encryptedCode = rc4Encryption.encryptCode(buttonCode);
        else
            encryptedCode = buttonCode;

        return  encryptedCode;
    }
}
