package com.example.akhil.decode;


import android.util.Log;

/**
 * Created by akhil on 11/3/17.
 */

public class EncryptionKeys {

    private static int RSApublickey;
    private static int RSAprivatekey;
    private static String RC4key = "";

    public EncryptionKeys() {

        this.RSApublickey = 3337;
        this.RSAprivatekey = 79;
        Log.d("Constructor","ENCRYPTIONKEYS");
    }

    public int getRSApublickeykey() { return RSApublickey; }

    public int getRSAprivatekey() { return RSAprivatekey; }

    public String getRC4key() {
        return RC4key;
    }

    public void setRC4key(String RC4key) {
        this.RC4key = RC4key;
    }


}
