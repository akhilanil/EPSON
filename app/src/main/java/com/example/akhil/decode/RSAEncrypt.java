package com.example.akhil.decode;

import android.util.Log;

/**
 * Created by akhil on 22/3/17.
 */

public class RSAEncrypt {

    EncryptionKeys encryptionKeys;

    private int RSApublickey;
    private int RSAprivatekey;

    public RSAEncrypt(EncryptionKeys encryptionKeys) {
        this.encryptionKeys = encryptionKeys;
        this.RSAprivatekey = this.encryptionKeys.getRSAprivatekey();
        this.RSApublickey = this.encryptionKeys.getRSApublickeykey();
    }

    public String encryptPassword (String plainText) {

        String cipherText = "";

        /*
        * Delimiter '?' is used.
        * */
        Log.d("RSATEXT",plainText);

        for(int i = 0; i < plainText.length(); i++) {

            cipherText += encryptLetter(plainText.charAt(i)) + "?";

        }

        Log.d("RSACIPHER",cipherText);
        return cipherText;
    }


    private String encryptLetter(char text) {

        int textNumeric = text;

        int val = 0;
        int power,i,temp,mod,num;

        Log.d("RSALetter",String.valueOf(textNumeric));
        power = this.RSAprivatekey;
        mod = this.RSApublickey;
        val = num = textNumeric;

        for(i = 1; i<power; i++) {
            temp = num * val;
            val = temp % mod;
        }

        Log.d("RSALetterCipher",String.valueOf(val));

        return String.valueOf(val);


        //return String.valueOf(textNumeric);
    }





}
