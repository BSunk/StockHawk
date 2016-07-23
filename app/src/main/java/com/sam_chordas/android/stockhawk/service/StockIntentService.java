package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {
  Handler mHandler = new Handler(Looper.getMainLooper());

  public StockIntentService(){
    super(StockIntentService.class.getName());
    mHandler = new Handler();
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    if (intent.getStringExtra("tag").equals("add")) {
      args.putString("symbol", intent.getStringExtra("symbol"));
      int result = stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
      //result will be -1 if the addition of the stock fails. This will give the user the proper toast message to indicate that.
      if (result == -1) {
        mHandler.post(new DisplayToast(this, getString(R.string.alert_no_stock)));
      } else {
        mHandler.post(new DisplayToast(this, getString(R.string.alert_stock_added)));
      }
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    else {
      stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
    }
  }
}

