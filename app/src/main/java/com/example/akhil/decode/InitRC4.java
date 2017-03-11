package com.example.akhil.decode;

import android.util.Log;

/**
 * Created by akhil on 11/3/17.
 */

public class InitRC4 {

    String key = "";
    EncryptionKeys encryptionKeys;
    private static int[] s, prga;
    private int array_length = 15;


    public InitRC4(EncryptionKeys encryptionKeys) {

        this.encryptionKeys = encryptionKeys;
        this.key = encryptionKeys.getRC4key();
        s = new int[array_length];
        prga = new int[array_length];
        for(int i = 0; i < array_length; i++)
            s[i] = i;

        Log.d("Constructor","InitRC4");
    }

    private void doKSA() {

        int i,j=0,temp;

        for(i = 0; i < array_length; i++) {

            j = (j + s[i] + key.charAt(i%5)) % array_length;

            temp = s[i];
            s[i] = s[j];
            s[j] = temp;

        }
    }

    private void doPRGA() {

        int i,j,k,temp;
        i = j = k = 0;
        while(k < array_length ){

            i = (i + 1 ) % array_length;

            j = (j + s[i]) % array_length;

            temp = s[i];
            s[i] = s[j];
            s[j] = temp;

            prga[k] = s[(s[i] + s[j]) % array_length];

            k++;

        }
    }

    public void initialiseRC4() {
        doKSA();
        doPRGA();
    }

    public int [] getPrga() {

        return prga;
    }

}
