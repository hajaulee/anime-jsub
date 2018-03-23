package com.hajaulee.jsubanime;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by HaJaU on 04-03-18.
 */

public class LoadEpisodeList {
    static Movie movie;
    static VideoEnabledWebView webView;
    static Activity sender;
    public static final String LOAD_EP_SCRIPT = "javascript:(function loadEpisode() { " +
            "Android.sendLog('javascript');" +
            "if(document.querySelector('#episode-links') != null){" +
            "   var epi = document.querySelector('#episode-links');" +
            "   var a = epi.textContent || epi.innerText || '';" +
            "   Android.sendLog('Javascript running...');" +
            "   if(a.length > 10){" +
            "       Android.createEpList(a);" +
            "   }else{" +
            "       Android.sendLog('Reload episode list.');" +
            "       setTimeout(loadEpisode, 50);" +
            "   }" +
            "}else{" +
            "   Android.sendLog('Null episode list.');" +
            "   setTimeout(loadEpisode, 100);" +
            "}" +
            "})()";

    public static void load(Movie m){
        movie = m;
    }
    public static void load() {

        sender = MainActivity.getInstance();
        movie = MovieDetailsView.getInstance().getSelectedMovie();
        webView = new VideoEnabledWebView(sender);

        webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36");
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new AndroidAPI(sender, webView, movie), "Android");
        webView.loadUrl(movie.getFirstEpisodeLink());
        Log.d("zzz:start", webView.getUrl());
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // view.loadUrl(url);
                if (url.contains("www.anjsub.com")) {
                    webView.loadUrl(url);
                    Log.d("zzz:OverrideLoad", "Redirect to: " + url);
                }
                Log.d("zzz:OverrideLoad", url);
                return true;
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {

                Log.d("zzz:Commit", url);
                super.onPageCommitVisible(view, url);
                Log.d("zzz:Commit", "");
            }

            @Override
            public void onPageFinished(WebView w, String url) {
                super.onPageFinished(w, url);

                Log.d("zzz:Finished", url);
                webView.loadUrl(LOAD_EP_SCRIPT);
            }
        });
    }
}
