package com.hajaulee.jsubanime;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class PreloadNextEpisode {
    private static VideoEnabledWebView webView;
    private static Movie thisMovie;
    private static CharSequence episode;
    public static boolean LOADED = false;

    public static void preLoad(VideoEnabledWebPlayer ac, Movie movie) {
        LOADED = false;
        thisMovie = movie;
        episode = movie.getNextEp();
        webView = new VideoEnabledWebView(ac);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new AndroidAPI(ac), "Android");
//        webView.setVisibility(View.INVISIBLE);
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
                LOADED = true;
                webView.loadUrl(VideoEnabledWebPlayer.VIDEO_START_EVENT);
            }
        });
    }

    public static VideoEnabledWebView getWebView() {
        return webView;
    }

    public static boolean equals(Movie m) {
        return m.equals(thisMovie) && m.getCurrentEp().equals(episode);
    }
}
