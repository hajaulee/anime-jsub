/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;
    public static final int NUM_ROWS = 4;
    private static final int NUM_COLS = 15;

    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private String mBackgroundUri;
    private BackgroundManager mBackgroundManager;
    public ArrayList<List<Movie>> totalMovieList = new ArrayList<List<Movie>>(NUM_ROWS);

    private static ArrayObjectAdapter favoriteAdapter;
    private static Movie selectedMovie;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        for (int i = 0; i < NUM_ROWS; i++)
            totalMovieList.add(null);
        prepareBackgroundManager();

        setupUIElements();

        setupEventListeners();
        new LoadAnjsubData(this).load();
        Log.d(MovieList.FAVORITE_LIST, totalMovieList.size() + "");
        MovieList.loadFavoriteMovieList(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        startBackgroundTimer();

    }

    public static ArrayObjectAdapter getFavoriteAdapter() {
        return favoriteAdapter;
    }

    public void loadRows(int rowIndex, int itemIndex) {

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();
        MovieList.initCategory();
        ((MainActivity)getActivity()).hideHelloDialog();
        int i;
        for (i = 0; i < totalMovieList.size(); i++) {

            List<Movie> list = totalMovieList.get(i);

            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);

            for (int j = 0; j < list.size(); ++j) {
                listRowAdapter.add(list.get(j));
            }

            if (i == NUM_ROWS - 1)
                favoriteAdapter = listRowAdapter;
            else {
                listRowAdapter.add(String.valueOf(i));
            }
            HeaderItem header = new HeaderItem(i, MovieList.MOVIE_CATEGORY.get(i));
            ListRow listRow = new ListRow(header, listRowAdapter);
            mRowsAdapter.add(listRow);

        }

        HeaderItem gridHeader = new HeaderItem(i, MainActivity.getStringR(R.string.other));

        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        gridRowAdapter.add(getResources().getString(R.string.info));
        gridRowAdapter.add(getString(R.string.update));
        gridRowAdapter.add(getResources().getString(R.string.settings));
        mRowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));


        for (int j = 0; j < 3; j++)
            Log.d("updateMax:", MovieList.updateMax[j]);
        try {
            setAdapter(mRowsAdapter);
            if (itemIndex != 0)
                this.setSelectedPosition(rowIndex, false, new ListRowPresenter.SelectItemViewHolderTask(itemIndex));
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            Toast.makeText(getActivity(), "Đang nạp dữ liệu.", Toast.LENGTH_SHORT).show();
            Intent intent = getActivity().getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }


    }

    private void prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        // setBadgeDrawable(getActivity().getResources().getDrawable(
        // R.drawable.videos_by_google_banner));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);


        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
//                Toast.makeText(getActivity(), "Chức năng tìm phim chưa hoàn thiện", Toast.LENGTH_LONG)
//                        .show();
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    protected void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
                        mBackgroundManager.setDrawable(resource);
                    }
                });
        mBackgroundTimer.cancel();
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    public static Movie getSelectedMovie() {
        return selectedMovie;
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Movie) {
                Movie movie = (Movie) item;
                selectedMovie = movie;
                Log.d(TAG, movie.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.MOVIE, movie);
                Log.d("zszobject", movie.hashCode() + "");
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            } else if (item instanceof String) {
                if (((String) item).contains(getString(R.string.info))) {
                    ((MainActivity) getActivity()).showInfoDialog();
                } else if (((String) item).contains(getString(R.string.update))) {
                    ((MainActivity) getActivity()).showCheckUpdateDialog();
                } else if (((String) item).contains(getString(R.string.settings))) {
                    ((MainActivity)getActivity()).showSettingsDialog();
                } else {
                    Toast.makeText(getActivity(), MainActivity.getStringR(R.string.wait), Toast.LENGTH_SHORT).show();
                    switch ((String) item) {
                        case "0":
                            new LoadAnjsubData(MainFragment.this)
                                    .load(0, "https://www.anjsub.com/search?updated-max=" + MovieList.updateMax[0] + "T00%3A25%3A00-08%3A00&max-results=9");
                            break;
                        case "1":
                            new LoadAnjsubData(MainFragment.this)
                                    .load(1, "https://www.anjsub.com/search/label/Comedy?updated-max=" + MovieList.updateMax[1] + "T00%3A28%3A00-08%3A00&max-results=9");
                            break;
                        case "2":
                            new LoadAnjsubData(MainFragment.this)
                                    .load(2, "https://www.anjsub.com/search/label/Sci-Fi?updated-max=" + MovieList.updateMax[2] + "T00%3A37%3A00-07%3A00&max-results=9");
                            break;
                    }
                }
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Movie) {
                mBackgroundUri = ((Movie) item).getBackgroundImageUrl();
                startBackgroundTimer();
            }
        }
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateBackground(mBackgroundUri);
                }
            });
        }
    }

    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(getResources().getColor(R.color.default_background));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }

}
