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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.app.DetailsFragmentBackgroundController;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.Collections;
import java.util.List;

/*
 * LeanbackDetailsFragment extends DetailsFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related videos.
 */
public class VideoDetailsFragment extends DetailsFragment {
    private static final String TAG = "VideoDetailsFragment";

    private static final int ACTION_WATCH_NOW = 1;
    private static final int ACTION_WATCH_PREVIOUS = 2;
    private static final int ACTION_WATCH_NEXT = 3;

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 390;

    private static final int NUM_COLS = 10;

    private Movie mSelectedMovie;

    private ArrayObjectAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;

    private DetailsFragmentBackgroundController mDetailsBackground;
    private ArrayObjectAdapter actionAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);
        mDetailsBackground = new DetailsFragmentBackgroundController(this);

        mSelectedMovie =
                (Movie) getActivity().getIntent().getSerializableExtra(DetailsActivity.MOVIE);
        mSelectedMovie = MovieList.getMovieFromTotalList(mSelectedMovie);
        mSelectedMovie = MovieList.getMovieFromFavoriteList(mSelectedMovie);

        if (mSelectedMovie != null) {
            mPresenterSelector = new ClassPresenterSelector();
            mAdapter = new ArrayObjectAdapter(mPresenterSelector);
            setupDetailsOverviewRow();

            setupDetailsOverviewRowPresenter();
            setupRelatedMovieListRow();
            setAdapter(mAdapter);
            initializeBackground(mSelectedMovie);
            setOnItemViewClickedListener(new ItemViewClickedListener());
        } else {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        createWatchActionButton();
    }

    private void initializeBackground(Movie data) {
        mDetailsBackground.enableParallax();
        Glide.with(getActivity())
                .load(data.getBackgroundImageUrl())
                .asBitmap()
                .centerCrop()
                .error(R.drawable.default_background)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap,
                                                GlideAnimation<? super Bitmap> glideAnimation) {
                        mDetailsBackground.setCoverBitmap(bitmap);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }
                });
    }

    public static String timeFormat(int sec) {
        StringBuffer buffer = new StringBuffer();
        if (sec > 86400) {
            buffer.append((sec / 86400) + "d ");
            sec %= 86400;
        }
        if (sec > 3600) {
            buffer.append((sec / 3600) + ":");
            sec %= 3600;
        }
        buffer.append(String.format("%02d", (sec / 60)) + ":");
        sec %= 60;
        buffer.append(String.format("%02d", sec));
        return buffer.toString();
    }

    private void createWatchActionButton() {
        try {
            actionAdapter.clear();
            actionAdapter.add(
                    new Action(
                            ACTION_WATCH_NOW,
                            getResources().getString(R.string.watch_now),
                            (mSelectedMovie.getCurrentEp() != null) ? "Đang xem tập " +
                                    mSelectedMovie.getCurrentEp() +
                                    " lúc " +
                                    timeFormat(mSelectedMovie.getWatchingSecond())
                                    : "Nhấn yêu thích để lưu lịch sử xem!")
            );
            if (mSelectedMovie.getNextEp() != null)
                actionAdapter.add(
                        new Action(
                                ACTION_WATCH_NEXT,
                                getResources().getString(R.string.watch_next),
                                "Tập " + mSelectedMovie.getNextEp()));
            if (mSelectedMovie.getPreviousEp() != null)
                actionAdapter.add(
                        new Action(
                                ACTION_WATCH_PREVIOUS,
                                getResources().getString(R.string.watch_previous), "Tập " + mSelectedMovie.getPreviousEp()));

            actionAdapter.notify();

        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    private void setupDetailsOverviewRow() {
        Log.d(TAG, "doInBackground: " + mSelectedMovie.toString());
        final DetailsOverviewRow row = new DetailsOverviewRow(mSelectedMovie);
        row.setImageDrawable(
                ContextCompat.getDrawable(getActivity(), R.drawable.default_background));
        int width = convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_WIDTH);
        int height = convertDpToPixel(getActivity().getApplicationContext(), DETAIL_THUMB_HEIGHT);
        Glide.with(getActivity())
                .load(mSelectedMovie.getCardImageUrl())
                .centerCrop()
                .error(R.drawable.default_background)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
                        Log.d(TAG, "details overview card image url ready: " + resource);
                        row.setImageDrawable(resource);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }
                });

        actionAdapter = new ArrayObjectAdapter();
        createWatchActionButton();
        row.setActionsAdapter(actionAdapter);
        mAdapter.add(row);
    }


    private void setupDetailsOverviewRowPresenter() {
        // Set detail background.
        FullWidthDetailsOverviewRowPresenter detailsPresenter =
//                new FullWidthDetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
                new FullWidthDetailsOverviewRowPresenter(new MovieDetailsView());
        detailsPresenter.setBackgroundColor(
                ContextCompat.getColor(getActivity(), R.color.selected_background));

        // Hook up transition element.
        FullWidthDetailsOverviewSharedElementHelper sharedElementHelper =
                new FullWidthDetailsOverviewSharedElementHelper();
        sharedElementHelper.setSharedElementEnterTransition(
                getActivity(), DetailsActivity.SHARED_ELEMENT_NAME);
        detailsPresenter.setListener(sharedElementHelper);
        detailsPresenter.setParticipatingEntranceTransition(true);

        detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {

                Intent intent = new Intent(getActivity(), VideoEnabledWebPlayer.class);
                intent.putExtra(DetailsActivity.MOVIE, mSelectedMovie);
                switch ((int) action.getId()) {
                    case ACTION_WATCH_NOW:
                        intent.putExtra(DetailsActivity.EPISODE, mSelectedMovie.getCurrentEp());
                        if (mSelectedMovie.getWatchingSecond() > 10)
                            MainActivity.showWatchInMiddleConfirmationDialog(VideoDetailsFragment.this, intent, mSelectedMovie);
                        else startActivity(intent);
                        break;
                    case ACTION_WATCH_NEXT:
                        intent.putExtra(DetailsActivity.EPISODE, mSelectedMovie.getNextEp());
                        startActivity(intent);
                        break;
                    case ACTION_WATCH_PREVIOUS:
                        intent.putExtra(DetailsActivity.EPISODE, mSelectedMovie.getPreviousEp());
                        startActivity(intent);
                        break;
                }
            }
        });
        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
    }

    private void setupRelatedMovieListRow() {
        String subcategories[] = {"Yêu thích"};
        List<Movie> list = MovieList.favoriteMovies;

        Collections.shuffle(list);
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        for (int j = 0; j < list.size(); j++) {
            listRowAdapter.add(list.get(j));
        }

        HeaderItem header = new HeaderItem(0, subcategories[0]);
        mAdapter.add(new ListRow(header, listRowAdapter));
        mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
    }

    public int convertDpToPixel(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Movie) {
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(getResources().getString(R.string.movie), (Movie) item);

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            }
        }
    }
}
