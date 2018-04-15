package com.hajaulee.jsubanime;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SpeechRecognitionCallback;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by HaJaU on 26-03-18.
 */

public class SearchFragment extends android.support.v17.leanback.app.SearchFragment
        implements android.support.v17.leanback.app.SearchFragment.SearchResultProvider {

    private static final String TAG = SearchFragment.class.getSimpleName();

    private static final int REQUEST_SPEECH = 0x00000010;
    public static final int INDEX = 10;
    public static final int TEST_SEARCH = 11;

    public ArrayObjectAdapter getListRowAdapter() {
        return listRowAdapter;
    }

    private ArrayObjectAdapter listRowAdapter;


    private ArrayObjectAdapter mRowsAdapter;

    public static List<Movie> searchResult; // Has index is 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        setSearchResultProvider(this);


        setOnItemViewClickedListener(new ItemViewClickedListener());


        if (MainActivity.hasPermissions(getActivity(), Manifest.permission.RECORD_AUDIO)) {
            // SpeechRecognitionCallback is not required and if not provided recognition will be handled
            // using internal speech recognizer, in which case you must have RECORD_AUDIO permission
            setSpeechRecognitionCallback(new SpeechRecognitionCallback() {
                @Override
                public void recognizeSpeech() {
                    Log.v(TAG, "recognizeSpeech");
                    //Toast.makeText(getActivity(), "recognizeSpeech", Toast.LENGTH_SHORT).show();
                    try {
                        startActivityForResult(getRecognizerIntent(), REQUEST_SPEECH);
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "Cannot find activity for speech recognizer", e);
                        Toast.makeText(getActivity(), "Cannot find activity for speech recognizer", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SPEECH:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        setSearchQuery(data, true);
                        break;
                    case RecognizerIntent.RESULT_CLIENT_ERROR:
                        Log.d("RESULT_CLIENT_ERROR", String.valueOf(requestCode));
                        break;
                }
        }
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        Log.d(TAG, "getResultsAdapter");
        Log.d(TAG, mRowsAdapter.toString());

        // It should return search result here,
        // but static Movie Item list will be returned here now for practice.
        List<Movie> mItems = searchResult;
        if(mItems == null)
            mItems = MovieList.favoriteMovies;
        listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        listRowAdapter.addAll(0, mItems);
        HeaderItem header = new HeaderItem("Kết quả tìm kiếm");
        mRowsAdapter.add(new ListRow(header, listRowAdapter));
        //Toast.makeText(getActivity(), "Search end", Toast.LENGTH_SHORT).show();
        return mRowsAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        Log.i(TAG, String.format("Search Query Text Change %s", newQuery));
        //Toast.makeText(getActivity(),"Change: " + newQuery, Toast.LENGTH_SHORT).show();
        return true;
    }

    public void setSearchResultAdapter(List<Movie> l){
        synchronized (listRowAdapter) {
            searchResult = l;
            listRowAdapter.clear();
            listRowAdapter.addAll(0, l);
            listRowAdapter.notify();
        }
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(TAG, String.format("Search Query Text Submit %s", query));
        //Toast.makeText(getActivity(), "Commit: " + query, Toast.LENGTH_SHORT).show();
        listRowAdapter.clear();
        new LoadAnjsubData(SearchFragment.this)
                .load(INDEX, "https://www.anjsub.com/search/?q=" + query);
        Log.d("Search::", "start seach" + query);
        return true;
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Movie) {
                Movie movie = (Movie) item;
                Log.d(TAG, movie.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, movie);
                Log.d("zszobject", movie.hashCode() + "");
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            }
        }
    }
}

