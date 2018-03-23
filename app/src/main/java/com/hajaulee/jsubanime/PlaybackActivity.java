/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.hajaulee.jsubanime;

import android.os.Bundle;
import android.support.v17.leanback.app.VideoSupportFragmentGlueHost;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import java.util.Date;

/**
 * Loads {@link PlaybackVideoFragment}.
 */
public class PlaybackActivity extends FragmentActivity {

//    private PlaybackVideoFragment videoFragment;
    private PlaybackVideoFragment videoFragment;
    private long dispatchTouchLastTime = 0;
    final float MIN_DISTANCE = 100f;
    private float x1 = 0, x2 = 0, y1 = 0, y2 = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Keep screen on and Fullscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_example);
//        videoFragment = new PlaybackVideoFragment();
        videoFragment = new PlaybackVideoFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, videoFragment)
                    .commit();
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                Log.d("dispatchTouchEvent", "DOWN::\t\t" + x1 + "|" + y1);
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();
                Log.d("dispatchTouchEvent", "UP::\t\t" + x2 + "|" + y2);
                boolean isSWipe = false;
                boolean isVerticalSwipe = false;
                boolean isHorizontalSwipe = false;
                float deltaX = x2 - x1;
                float deltaY = y2 - y1;
                Log.d("dispatchTouchEvent", "delta::\t\tX:" + deltaX + "|Y:" + deltaY + "|MIN:" + MIN_DISTANCE);
                if(Math.abs(deltaX) > MIN_DISTANCE)
                    isHorizontalSwipe = true;
                if(Math.abs(deltaY) > MIN_DISTANCE)
                    isVerticalSwipe = true;
                isSWipe = (isHorizontalSwipe || isVerticalSwipe);

                if (isSWipe) {
                    Log.d("dispatchTouchEvent", "SWIPE");
                } else {
                    // is click
                    Log.d("dispatchTouchEvent", "CLICK");
                    long dispatchTouchCurrentTime = new Date().getTime();
                    Log.d("dispatchTouchEvent", "" + dispatchTouchCurrentTime);
                    if (dispatchTouchCurrentTime - 500 > dispatchTouchLastTime) {
                        VideoSupportFragmentGlueHost glueHost = videoFragment.glueHost;
                        if (glueHost.isControlsOverlayVisible())
                            glueHost.hideControlsOverlay(true);
                        else
                            glueHost.showControlsOverlay(true);
                    } else {
                        Log.d("dispatchTouchEvent", "Delay not enought");
                    }
                    dispatchTouchLastTime = dispatchTouchCurrentTime;
                }
                break;
        }

        return super.dispatchTouchEvent(event);
    }
}