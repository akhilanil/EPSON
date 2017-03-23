package com.example.akhil.decode;

import android.util.Log;

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

    public String getCode(String buttonPressed) {
        String encryptedCode = "";
        String buttonCode = "";

        buttonCode = remoteCode.getRemoteCode(buttonPressed);

        Log.d("CODE",buttonCode);

        encryptedCode = rc4Encryption.encryptCode(buttonCode);


        return  encryptedCode;
    }
}
