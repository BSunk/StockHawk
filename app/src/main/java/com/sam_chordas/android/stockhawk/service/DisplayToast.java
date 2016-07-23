package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by Bharat on 7/22/2016.
 */
//class to display a toast message from the service intent.
public class DisplayToast implements Runnable {
    private final Context mContext;
    String mText;

    public DisplayToast(Context mContext, String text){
        this.mContext = mContext;
        mText = text;
    }

    public void run(){
        Toast alert = Toast.makeText(mContext, mText, Toast.LENGTH_SHORT);
        alert.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
        alert.show();
    }
}
