package com.arahasya.phonetime;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;


public class TimerService extends Service {

    private WindowManager windowManager;
    WindowManager.LayoutParams params, params1;
    Chronometer chronometer;
    ImageView deleteView,box;
    BroadcastReceiver mReceiver;
    long timeWhenStopped;
    IntentFilter filter;


    int LAYOUT_FLAG;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();

        filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenUnlockReceiver();

        deleteView = new ImageView(this);

        deleteView.setImageResource(R.drawable.ic_cancel);

        chronometer = new Chronometer(this);
        chronometer.setTextSize(35);


        chronometer.setBackgroundResource(R.drawable.ic_green_box);


        //chronometer.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        //chronometer.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // chatHead = new ImageView(this);
        // chatHead.setImageResource(R.mipmap.ic_launcher);
        params = setParams();
        Log.i("Params", String.valueOf(params));


        params1 = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,

                PixelFormat.TRANSLUCENT);
        params1.height = 200;
        params1.width = 200;

        params1.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        params1.y = Gravity.BOTTOM - 10;


        windowManager.addView(deleteView, params1);
        deleteView.setVisibility(View.INVISIBLE);


        //this code is for dragging the chat head
        chronometer.setOnTouchListener(new View.OnTouchListener() {


            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                deleteView.setVisibility(View.VISIBLE);
                if (isViewOverlapping(deleteView, chronometer)) {
                    // Log.i("Params", String.valueOf(params));
                    params = setParams();
                    chronometer.setVisibility(View.INVISIBLE);

                    windowManager.updateViewLayout(chronometer, params);
                   // Log.i("Params", String.valueOf(params));
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            chronometer.setVisibility(View.VISIBLE);
                        }
                    }, 60000);
                }

                //windowManager.removeView(chronometer);


                //chronometer.setVisibility(View.INVISIBLE);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        deleteView.setVisibility(View.INVISIBLE);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX
                                + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY
                                + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(chronometer, params);
                        return true;


                }
                return false;
            }


        });

        //windowManager.addView(textTimer,params);
        windowManager.addView(chronometer, params);




    }

    private boolean isViewOverlapping(View firstView, View secondView) {
        int[] firstPosition = new int[2];
        int[] secondPosition = new int[2];

        firstView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        firstView.getLocationOnScreen(firstPosition);
        secondView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        secondView.getLocationOnScreen(secondPosition);

        return firstPosition[0] < secondPosition[0] + secondView.getMeasuredWidth()
                && firstPosition[0] + firstView.getMeasuredWidth() > secondPosition[0]
                && firstPosition[1] < secondPosition[1] + secondView.getMeasuredHeight()
                && firstPosition[1] + firstView.getMeasuredHeight() > secondPosition[1];
    }


    @Override
    public void onDestroy() {


        try {
            if (mReceiver != null)
                unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (chronometer != null) {
            windowManager.removeView(chronometer);
        }

        super.onDestroy();


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(mReceiver, filter);
        return super.onStartCommand(intent, flags, startId);
    }


    public class ScreenUnlockReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                // start the service
                //  Log.i("TIME", String.valueOf(SystemClock.elapsed));

                long elapsedMillis = (SystemClock.elapsedRealtime() - chronometer.getBase());
                if(elapsedMillis>7200000){
                    chronometer.setBackgroundResource(R.drawable.ic_red_box);
                }else if(elapsedMillis>3600000){

                    chronometer.setBackgroundResource(R.drawable.ic_orange_box);
                }
                chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                chronometer.start();

                Log.i("SCREEN", "ON");
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {

                chronometer.stop();
                timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
                Log.i("SCREEN", "off");
            }
        }


    }

    public WindowManager.LayoutParams setParams() {
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,

                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;
        return params;
    }


}