package com.hajaulee.jsubanime;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Date;

public class VideoEnabledWebPlayer extends Activity {

    public static boolean SHOW_WEBVIEW = false;


    private long dispatchTouchLastTime = 0;
    final float MIN_DISTANCE = 100f;
    private float x1 = 0, x2 = 0, y1 = 0, y2 = 0;

    private VideoEnabledWebView webView;
    public Movie movie;
    private CharSequence episode;
    public ProgressDialog dialog;
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
            "document.querySelector('.vjs-play-control.vjs-control.vjs-button').click();" +
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
    private String VIDEO_START_EVENT = "javascript:(function(){" +
            "document.querySelector('#videoPlayer_html5_api').addEventListener('playing',myHandler,false);" +
            "    function myHandler(e) {" +
            "        Android.showVideo();" +
            "    }" +
            "})()";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_example);
        SHOW_WEBVIEW = false;

        movie = (Movie) this.getIntent().getSerializableExtra(DetailsActivity.MOVIE);
        movie = MovieList.getMovieFromFavoriteList(movie);
        episode = (CharSequence) this.getIntent().getSerializableExtra(DetailsActivity.EPISODE);


        movie.setCurrentEp((episode == null) ? null : episode.toString());
        movie.setWatchingSecond(0);
        MovieList.saveFavoriteMovieList(null, MovieList.SaveAction.REMOVE);

        dialog = new ProgressDialog(this);
        dialog.setMessage(Html.fromHtml("<font color=\"black\">Đang chuẩn bị video...</font>"));
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.background_light);

        // Save the web view
        webView = (VideoEnabledWebView) findViewById(R.id.webView);
        webView.setVisibility(View.INVISIBLE);

        // Initialize the VideoEnabledWebChromeClient and set event handlers
        View nonVideoLayout = findViewById(R.id.nonVideoLayout); // Your own view, read class comments
        ViewGroup videoLayout = (ViewGroup) findViewById(R.id.videoLayout); // Your own view, read class comments
        //noinspection all
        View loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null); // Your own view, read class comments
        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView) // See all available constructors...
        {
            // Subscribe to standard events, such as onProgressChanged()...
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Your code...

                Log.d("xxxonProgressChanged", String.valueOf(webView.getVisibility()));
            }
        };

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
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }

                    Toast.makeText(getApplicationContext(), "Xem toàn màn hình", Toast.LENGTH_SHORT).show();
//                    showWebView(SHOW_WEBVIEW);
                    webView.loadUrl(VIDEO_FINISH_EVENT);

                } else {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
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
        webView.loadUrl(movie.getFirstEpisodeLink() + "?v=episode" + episode + "&m=1");
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
                webView.loadUrl("javascript:(function(){" +
                        "Android.showWebView();" +
                        "document.querySelector('.vjs-big-play-button').click();" +
                        "document.querySelector('.vjs-fullscreen-control').click();" +
                        "document.querySelector('#videoPlayer > div.vjs-control-bar > div.vjs-captions-button.vjs-menu-button.vjs-menu-button-popup.vjs-control.vjs-button > div > ul > li:nth-child(3)').click();" +
                        "})()");
                webView.loadUrl(LoadEpisodeList.LOAD_EP_SCRIPT);
                webView.loadUrl(VIDEO_START_EVENT);
            }
        });


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
    public void onBackPressed() {
        webView.loadUrl(CURRENT_PLAY_TIME);
        // Notify the VideoEnabledWebChromeClient, and handle it ourselves if it doesn't handle it
        if (!webChromeClient.onBackPressed()) {
            if (webView.canGoBack()) {
                //webView.goBack();   // Not need webview goBack
            } else {
                // Standard back button implementation (for example this could close the app)
//                super.onBackPressed();
            }
            super.onBackPressed();
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
                    // is click
                    Log.d("dispatchTouchEvent", "CLICK");
                    long dispatchTouchCurrentTime = new Date().getTime();
                    Log.d("dispatchTouchEvent", "" + dispatchTouchCurrentTime);
                    if (dispatchTouchCurrentTime - 500 > dispatchTouchLastTime) {
//                        VideoSupportFragmentGlueHost glueHost = videoFragment.glueHost;
//                        if (glueHost.isControlsOverlayVisible())
//                            glueHost.hideControlsOverlay(true);
//                        else
//                            glueHost.showControlsOverlay(true);
                        webView.loadUrl(PAUSE_PLAY);
                    } else {
                        Log.d("dispatchTouchEvent", "Delay not enought");
                    }
                    dispatchTouchLastTime = dispatchTouchCurrentTime;
                }
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    public void showWebView(boolean show) {
        if (show) {
            Log.d("xxxshowWebView", String.valueOf(webView.getVisibility()));
            SHOW_WEBVIEW = false;
            VideoEnabledWebPlayer.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    VideoEnabledWebPlayer.this.dialog.hide();
                    VideoEnabledWebPlayer.this.webView.setVisibility(View.VISIBLE);
                    VideoEnabledWebChromeClient.activityVideoView.setVisibility(View.VISIBLE);
                    Log.d("xxxrunOnUiThread", String.valueOf(webView.getVisibility()));
                }
            });

        }
    }


}
