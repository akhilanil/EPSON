package com.example.akhil.decode;


import android.util.Log;

/**
 * Created by akhil on 11/3/17.
 */

public class EncryptionKeys {

    private static String RSAkey = "";
    private static String RC4key = "";

    EncryptionKeys() {

        Log.d("Constructor","ENCRYPTIONKEYS");


    }

    public String getRSAkey() {
       return RSAkey;
    }

    public String getRC4key() {
        return RC4key;
    }

    public void setRC4key(String RC4key) {
        this.RC4key = RC4key;
    }

    public void setRSAkey(String RSAkey) {
        this.RSAkey  = RSAkey;
    }
}
