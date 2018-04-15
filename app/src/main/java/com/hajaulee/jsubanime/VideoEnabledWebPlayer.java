package com.hajaulee.jsubanime;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class VideoEnabledWebPlayer extends Activity {

    private long touchLastTime = 0;

    private boolean LOADED = false;
    private boolean DOUBLE_BACK_PRESSED = false;
    private boolean isFullScreen = false;
    private boolean seek = false;
    final int DELAY = 300;
    final float MIN_DISTANCE = 100f;
    private float x1 = 0, x2 = 0, y1 = 0, y2 = 0;
    private int pressCount = 3;
    private VideoEnabledWebView webView;
    public Movie movie;
    private CharSequence episode;
    static public ProgressDialog dialog;
    private VideoEnabledWebChromeClient webChromeClient;

    private String BACKWARD = "javascript: (function(){" +
            "var v= document.querySelector('#videoPlayer_html5_api');" +
            "v.currentTime -= 10;" +
            "})()";
    private String FORWARD = "javascript: (function(){" +
            "var v= document.querySelector('#videoPlayer_html5_api');" +
            "v.currentTime += 30;" +
            "})()";
    private String PAUSE_PLAY = "javascript:(function(){" +
            "   document.querySelector('.vjs-play-control.vjs-control.vjs-button').click();" +
            "   if(document.querySelector('#videoPlayer_html5_api').paused){" +
            "       document.querySelector('#videoPlayer > div.vjs-control-bar').style.opacity = 50;" +
            "   }else{" +
            "       document.querySelector('#videoPlayer > div.vjs-control-bar').style.opacity = 0;" +
            "   }" +
            "})()";

    private String CURRENT_PLAY_TIME = "javascript:(function(){" +
            "var v= document.querySelector('#videoPlayer_html5_api');" +
            "Android.setCurrentTime(v.currentTime);" +
            "})()";
    private String VIDEO_FINISH_EVENT = "javascript:(function(){" +
            "document.querySelector('#videoPlayer_html5_api').addEventListener('ended',myHandler,false);" +
            "    function myHandler(e) {" +
            "        Android.nextEp();" +
            "    }" +
            "})()";
    static final String VIDEO_START_EVENT = "javascript:(function(){" +
            "       Android.sendLog('Logg');" +
            "    function myHandler(e) {" +
            "        Android.sendLog('Ajjj');" +
            "        Android.showVideo();" +
            "    }" +
            "document.querySelector('#videoPlayer_html5_api').addEventListener('playing',myHandler,false);" +
//            "document.getElementsByTagName('video')[0].addEventListener('playing',myHandler,false);" +
            "})()";
    private RelativeLayout nonVideoLayout;
    private ViewGroup videoLayout;
    private View loadingView;
    private Integer watchTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.setProperty("http.keepAlive", "false");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_player);

        movie = (Movie) this.getIntent().getSerializableExtra(DetailsActivity.MOVIE);
        movie = MovieList.getMovieFromFavoriteList(movie);
        episode = (CharSequence) this.getIntent().getSerializableExtra(DetailsActivity.EPISODE);
        watchTime = (Integer) this.getIntent().getSerializableExtra(DetailsActivity.WATCH_TIME);
        if (watchTime == null) {
            watchTime = 0;
        } else {
            seek = true;
        }
        movie.setCurrentEp((episode == null) ? null : episode.toString());
        movie.setWatchingSecond(0);
        MovieList.saveFavoriteMovieList(null, MovieList.SaveAction.REMOVE);

        dialog = new ProgressDialog(this);
        dialog.setMessage(Html.fromHtml("<font color=\"black\">Đang chuẩn bị video...</font>"));
        dialog.setCancelable(true);
        dialog.setInverseBackgroundForced(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.background_light);
        dialog.show();

        // Init webView

        if (PreloadNextEpisode.equals(movie) && PreloadNextEpisode.LOADED) {
            LOADED = true;
            webView = PreloadNextEpisode.getWebView();
        } else {
            LOADED = false;
            webView = new VideoEnabledWebView(this);
        }
        PreloadNextEpisode.preLoad(this, movie);
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
//        webView.setVisibility(View.VISIBLE);

        // Initialize the VideoEnabledWebChromeClient and set event handlers
        nonVideoLayout = findViewById(R.id.nonVideoLayout); // Your own view, read class comments
        nonVideoLayout.removeAllViews();
        nonVideoLayout.addView(webView);

        videoLayout = (ViewGroup) findViewById(R.id.videoLayout); // Your own view, read class comments
        //noinspection all
        loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null); // Your own view, read class comments

        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView) // See all available constructors...
        {
            // Subscribe to standard events, such as onProgressChanged()...
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Your code...

                Log.d("xxxonProgressChanged", String.valueOf(webView.getVisibility()));
            }
        };
        webChromeClient.setWebPlayer(this);
        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
            @Override
            public void toggledFullscreen(boolean fullscreen) {
                Log.d("xxxtoggledFullscreen", String.valueOf(webView.getVisibility()));
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen) {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    //noinspection all
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

                    isFullScreen = true;
                    Toast.makeText(getApplicationContext(), "Xem toàn màn hình", Toast.LENGTH_SHORT).show();
                    webView.loadUrl(VIDEO_FINISH_EVENT);
//                    runScriptOnWebFinish();

                } else {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }

            }
        });
        webView.setWebChromeClient(webChromeClient);

        // Call private class InsideWebViewClient
        webView.setWebViewClient(new InsideWebViewClient());
        webView.addJavascriptInterface(new AndroidAPI(this), "Android");
        // Navigate anywhere you want, but consider that this classes have only been tested on YouTube's mobile site
//        webView.loadUrl("http://www.anjsub.com/p/inu-x-boku-ss.html?v=episode01&m=1");
        if (episode != null) {
            if (episode.length() == 1)
                episode = "0" + episode;
        } else {
            episode = "01";
        }

        if (LOADED) {
            runScriptOnWebFinish();
            Log.d("java", "aaass");
        } else {
            webView.loadUrl(movie.getFirstEpisodeLink() + "?v=episode" + episode + "&m=1");
        }
        Log.d("xxxExeahihi:", movie.getFirstEpisodeLink());

        webView.setWebViewClient(new WebViewClient() {


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // view.loadUrl(url);
                if (url.contains("www.anjsub.com/p")) {
                    webView.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView w, String url) {
                super.onPageFinished(w, url);
                Log.d("xxxonPageFinished", webView.getUrl());
                runScriptOnWebFinish();
            }
        });


    }

    public void runScriptOnWebFinish() {

        webView.loadUrl("javascript:(function(){" +
                "document.querySelector('.vjs-big-play-button').click();" +
                "Android.sendLog(''+document.querySelector('#videoPlayer').clientHeight);" +
                "Android.sendLog(''+window.innerHeight);" +
                "if( document.querySelector('#videoPlayer').clientHeight == window.innerHeight) {}else{" +
                "document.querySelector('.vjs-fullscreen-control').click();" +
                "Android.sendLog('Not fullscreen');}" +
                "document.querySelector('#videoPlayer > div.vjs-control-bar > " +
                "           div.vjs-captions-button.vjs-menu-button.vjs-menu-button-popup.vjs-control.vjs-button >" +
                "           div > ul > li:nth-child(3)').click();" +
                "   document.querySelector('.vjs-fullscreen-control').style.visibility = 'hidden';" +
                "   document.querySelector('.vjs-captions-button').style.visibility = 'hidden';" +
                "})()");

        webView.loadUrl(VIDEO_START_EVENT);
        webView.loadUrl(LoadEpisodeList.LOAD_EP_SCRIPT);
        if (seek) {
            webView.loadUrl("javascript: (function(){" +
                    "var v= document.querySelector('#videoPlayer_html5_api');" +
                    "v.currentTime = " + watchTime + ";" +
                    "})()");
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
//        Toast.makeText(this, String.valueOf(event.getKeyCode()), Toast.LENGTH_SHORT).show();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            webView.loadUrl(CURRENT_PLAY_TIME);

            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    webView.loadUrl(BACKWARD);
                    break;
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    webView.loadUrl(FORWARD);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    webView.loadUrl(PAUSE_PLAY);
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    new AndroidAPI(this).nextEp();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    new AndroidAPI(this).previousEp();
                    break;
                case KeyEvent.KEYCODE_BACK:
                    if (!DOUBLE_BACK_PRESSED) {
                        Toast.makeText(this, "Nhấn lần nữa để thoát.", Toast.LENGTH_SHORT).show();
                        webView.loadUrl(CURRENT_PLAY_TIME);
                        DOUBLE_BACK_PRESSED = true;
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                DOUBLE_BACK_PRESSED = false;
                            }
                        }, 2000);
                    } else {
                        webView.destroy();
                        this.finish();
                    }
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private class InsideWebViewClient extends WebViewClient {
        @Override
        // Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//             view.loadUrl(url);
            return true;
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
                if (Math.abs(deltaX) > MIN_DISTANCE)
                    isHorizontalSwipe = true;
                if (Math.abs(deltaY) > MIN_DISTANCE)
                    isVerticalSwipe = true;
                isSWipe = (isHorizontalSwipe || isVerticalSwipe);

                if (isSWipe) {
                    Log.d("dispatchTouchEvent", "SWIPE");
                    if (deltaX > 0) {// right swipe
                        webView.loadUrl(FORWARD);
                    } else {//left swipe
                        webView.loadUrl(BACKWARD);
                    }
                } else {
                    if (checkThreePress()) {
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        int width = displayMetrics.widthPixels;
                        if (x2 >= width / 2) {
                            new AndroidAPI(this).nextEp();
                        } else {
                            new AndroidAPI(this).previousEp();
                        }
                    }
                    // is click
                    Log.d("dispatchTouchEvent", "CLICK");
                    webView.loadUrl(PAUSE_PLAY);
                }
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    boolean checkThreePress() {
        long time = new Date().getTime();
        if (time > touchLastTime + DELAY) {
            pressCount = 2;
        } else {
            pressCount--;
        }
        //Toast.makeText(this, "" + pressCount, Toast.LENGTH_SHORT).show();
        touchLastTime = time;
        return pressCount <= 0;
    }

    public void showWebView() {
//        Log.d("xxxshowWebView", String.valueOf(webView.getVisibility()));
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showWebViewAction();
            }
        });
    }

    public void showWebViewAction() {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("Runnable", "Dismiss dialog:");
                if (isFullScreen || !dialog.isShowing()) {
                    VideoEnabledWebPlayer.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.hide();
                            dialog.dismiss();
                        }
                    });
                    this.cancel();
                }
            }
        }, 0, 100);

        Log.d("xxxRunOnUiThread", String.valueOf(movie.getCurrentEp() + ":" + webView.getVisibility()));
    }

}
