package com.hajaulee.jsubanime;

import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;


/**
 * Created by HaJaU on 08-03-18.
 */

public class AndroidAPI {
    Activity activity;
    WebView webView;
    Movie movie;

    public AndroidAPI(Activity ac) {
        activity = ac;
    }

    public AndroidAPI(Activity ac, WebView webView) {
        activity = ac;
        this.webView = webView;
    }

    public AndroidAPI(Activity ac, WebView webView, Movie movie) {
        activity = ac;
        this.webView = webView;
        this.movie = movie;
    }

    @JavascriptInterface
    public void createEpList(String epList) {

        final MovieDetailsView mdv = MovieDetailsView.getInstance();
        movie = (movie != null) ? movie : mdv.getSelectedMovie();
        Movie movie1 = MainFragment.getSelectedMovie();
        ViewGroup listEp = mdv.getEpisodeListView();

        Log.d("zzz:creaer", "start");
        epList = Html.fromHtml(epList).toString();
        Log.d("zzz:Episodexx", " " + epList);
        String[] arrayEp = epList.replaceAll("( | )+", MovieDetailsView.SEPARATOR).split(MovieDetailsView.SEPARATOR);

        movie.setEpisodeList(arrayEp);
        if (MovieList.isLiked(movie)) {
            MovieList.getMovieFromFavoriteList(movie).setEpisodeList(arrayEp);
            MovieList.saveFavoriteMovieList(null, MovieList.SaveAction.REMOVE);
        }
        if (movie1.equals(movie))
            movie1.setEpisodeList(movie.getEpisodeList());
        if (listEp != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mdv.addEpisodeButton();
                }
            });
        }

        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
    }

    @JavascriptInterface
    public void message(String s) {
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void setCurrentTime(String s) {
        int currentTime = Integer.parseInt(s);
        ((VideoEnabledWebPlayer) activity).movie.setWatchingSecond(currentTime);
    }

    @JavascriptInterface
    public void setCurrentTime(double s) {
        int currentTime = (int) s;
        ((VideoEnabledWebPlayer) activity).movie.setWatchingSecond(currentTime);
        MovieList.saveFavoriteMovieList(null, MovieList.SaveAction.REMOVE);
    }

    @JavascriptInterface
    public void sendLog(String s) {
        Log.d("zzz:Javascript:", s);
    }

    @JavascriptInterface
    public void nextEp() {
        Intent intent = new Intent(DetailsActivity.getInstance(), VideoEnabledWebPlayer.class);
        Movie mSelectedMovie = MovieDetailsView.getInstance().getSelectedMovie();
        intent.putExtra(DetailsActivity.MOVIE, mSelectedMovie);
        intent.putExtra(DetailsActivity.EPISODE, mSelectedMovie.getNextEp());
        DetailsActivity.getInstance().startActivity(intent);

            message(String.format(MainActivity.getStringR(R.string.watch_next_ep),mSelectedMovie.getNextEp()));
        activity.finish();
    }

    @JavascriptInterface
    public void previousEp() {
        Intent intent = new Intent(DetailsActivity.getInstance(), VideoEnabledWebPlayer.class);
        Movie mSelectedMovie = MovieDetailsView.getInstance().getSelectedMovie();
        intent.putExtra(DetailsActivity.MOVIE, mSelectedMovie);
        intent.putExtra(DetailsActivity.EPISODE, mSelectedMovie.getPreviousEp());
        DetailsActivity.getInstance().startActivity(intent);
        message(String.format(MainActivity.getStringR(R.string.watch_previous_ep),mSelectedMovie.getPreviousEp()));
        activity.finish();
    }

    @JavascriptInterface
    public void showWebView() {

    }

    @JavascriptInterface
    public void showVideo() {
        sendLog("Bjjj");
        ((VideoEnabledWebPlayer) activity).showWebView();
        //Toast.makeText(activity,"///", Toast.LENGTH_SHORT).show();
    }
}