package com.hajaulee.jsubanime;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.support.v17.leanback.widget.Presenter;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by HaJaU on 28-02-18.
 */

public class MovieDetailsView extends Presenter {
    //        ViewHolder viewHolder =
    static public final String SEPARATOR = "_";
    static private int LIKE_BUTTON_WIDTH;
    static private int LIKE_BUTTON_HEIGHT;
    static private int EPISODE_BUTTON_SIZE;
    static private float SCALE = 1.2f;
    private Movie movie;
    private ViewGroup episodeListView;
    private static MovieDetailsView mdv;
    final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Button button = (Button) view;
            boolean isLikeButton = (button.getText().equals(MainActivity.getInstance().getString(R.string.like))
                    || button.getText().equals(MainActivity.getInstance().getResources().getString(R.string.unlike)));
            if (isLikeButton) {
                boolean like = button.getText().equals(MainActivity.getInstance().getString(R.string.like));
                if (like) {
                    Toast.makeText(MainActivity.getInstance(), MainActivity.getStringR(R.string.add_to_favorite), Toast.LENGTH_SHORT).show();
                    MovieList.addFavorite(movie);
                    button.setText(R.string.unlike);
                } else {
                    Toast.makeText(MainActivity.getInstance(), MainActivity.getStringR(R.string.delete_from_favorite), Toast.LENGTH_SHORT).show();
                    MovieList.removeFavorite(movie);
                    button.setText(R.string.like);
                }
            } else {
                Intent intent = new Intent(MainActivity.getInstance(), VideoEnabledWebPlayer.class);
                intent.putExtra(DetailsActivity.MOVIE, movie);
                intent.putExtra(DetailsActivity.EPISODE, button.getText());
                view.getContext().startActivity(intent);
            }
//            Toast.makeText(view.getContext(), "Chức năng chưa có", Toast.LENGTH_SHORT).show();
        }
    };
    final View.OnFocusChangeListener changeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {

            DisplayMetrics metrics = new DisplayMetrics();
            MainActivity.getInstance().getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            Button button = (Button) view;

            boolean isLikeButton = (button.getText().equals(MainActivity.getInstance().getString(R.string.like))
                    || button.getText().equals(MainActivity.getInstance().getResources().getString(R.string.unlike)));
            if (b) {
                button.setTextColor(MainActivity.getInstance().getResources().getColor(R.color.selected_background));
                if (isLikeButton)
                    changeButtonSize(button, LIKE_BUTTON_WIDTH * SCALE, LIKE_BUTTON_HEIGHT * SCALE);
                else {
                    if (button.getText().length() > 3)
                        changeButtonSize(button, 1.5f * EPISODE_BUTTON_SIZE * SCALE, EPISODE_BUTTON_SIZE * SCALE);
                    else
                        changeButtonSize(button, EPISODE_BUTTON_SIZE * SCALE, EPISODE_BUTTON_SIZE * SCALE);
                    LinearLayout listEpisode = (LinearLayout) button.getParent();
                    ScrollView scrollView = (ScrollView) listEpisode.getParent().getParent();
                    int scrollToX = button.getLeft() - listEpisode.getLeft() - 3 * EPISODE_BUTTON_SIZE;
                    if (scrollToX > 0)
                        listEpisode.scrollTo(scrollToX, 0);
//                        scrollView.smoothScrollTo(scrollToX, 0);
                    else
                        listEpisode.scrollTo(0, 0);
                }
            } else {
                button.setTextColor(MainActivity.getInstance().getResources().getColor(R.color.white));
                if (isLikeButton)
                    changeButtonSize(button, LIKE_BUTTON_WIDTH, LIKE_BUTTON_HEIGHT);
                else {
                    if (button.getText().length() > 3)
                        changeButtonSize(button, 1.5f * EPISODE_BUTTON_SIZE, EPISODE_BUTTON_SIZE);
                    else
                        changeButtonSize(button, EPISODE_BUTTON_SIZE, EPISODE_BUTTON_SIZE);
                }
            }
        }
    };


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent) {
        mdv = this;
        View view = getDetailsView();
        return new ViewHolder(view);
    }

    public Movie getSelectedMovie() {
        return movie;
    }

    public static MovieDetailsView getInstance() {
        return mdv;
    }

    public ViewGroup getEpisodeListView() {
        return episodeListView;
    }

    private void changeButtonSize(Button button, float sizeW, float sizeH) {
        ViewGroup.LayoutParams params = button.getLayoutParams();

        params.width = (int) sizeW;
        params.height = (int) sizeH;
        button.setLayoutParams(params);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
//            ((Button) viewHolder.view).setText(((Movie)item).getTitle());
        movie = (Movie) item;
        Log.d("Hashzzz", movie.hashCode() +"");
        movie = MovieList.getMovieFromTotalList(movie);
        movie = MovieList.getMovieFromFavoriteList(movie);
        Log.d("Hashzzz", movie.hashCode() +"");
        View view = viewHolder.view;

        TextView movieYear = view.findViewById(R.id.movie_year);
        TextView movieTitle = view.findViewById(R.id.movie_title);
        TextView movieGenres = view.findViewById(R.id.genres);
        TextView movieOverview = view.findViewById(R.id.overview);
        TextView movieDuration = view.findViewById(R.id.runtime);
        Button likeButton = view.findViewById(R.id.like);
        ProgressBar progressBar = view.findViewById(R.id.epi_loading_icon);

        movieTitle.setText(movie.getTitle());
        movieGenres.setText(movie.getGenres());
        movieDuration.setText(movie.getDuration());
        movieYear.setText("(" + movie.getYear() + ")");
        movieOverview.setText(Html.fromHtml(movie.getDescription()));

        if (MovieList.isLiked(movie)) {
            likeButton.setText(R.string.unlike);
        } else {
            likeButton.setText(R.string.like);
        }
        Log.d("zszobject", movie.hashCode() + "");
        episodeListView = view.findViewById(R.id.episode_list);

        if (movie.getEpisodeList() == null) {
            progressBar.setVisibility(View.VISIBLE);
            LoadEpisodeList.load();
        } else {
            addEpisodeButton();
        }


    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }

    public void addEpisodeButton() {

        ((ViewGroup) episodeListView.getParent()).getChildAt(0).setVisibility(View.GONE); // hide progress bar

        episodeListView.removeAllViews();
        for (int i = 1; i <= movie.getEpisodeCount(); i++) {
            episodeListView.addView(createEpisodeButton(this, movie.getEpisodeList()[i]));
        }
    }

    public float dp2px(int dp) {
        Resources r = MainActivity.getInstance().getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return px;
    }

    public static Button createEpisodeButton(MovieDetailsView mdv, String ep) {
        Button button = new Button(MainActivity.getInstance());
        ViewGroup.LayoutParams layoutParams;
        if (ep.length() > 3)
            layoutParams = new ViewGroup.LayoutParams((int) (1.5f * EPISODE_BUTTON_SIZE), EPISODE_BUTTON_SIZE);
        else
            layoutParams = new ViewGroup.LayoutParams(EPISODE_BUTTON_SIZE, EPISODE_BUTTON_SIZE);
        button.setLayoutParams(layoutParams);
        button.setFocusable(true);
        button.setFocusableInTouchMode(true);
        button.setOnClickListener(mdv.clickListener);
        button.setOnFocusChangeListener(mdv.changeListener);
        button.setText(String.valueOf(ep));
        return button;
    }

    public static void createEpisodeButton(Button button) {
        ViewGroup.LayoutParams layoutParams = button.getLayoutParams();
        button.setLayoutParams(layoutParams);
        button.setFocusable(true);
        button.setFocusableInTouchMode(true);
        button.setOnClickListener(mdv.clickListener);
        button.setOnFocusChangeListener(mdv.changeListener);
    }

    public View getDetailsView() {

        LayoutInflater inflater = (LayoutInflater) MainActivity
                .getInstance()
                .getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.vh_details, null);

        Button like = v.findViewById(R.id.like);
        episodeListView = v.findViewById(R.id.episode_list);
        episodeListView.setScrollContainer(true);
        LIKE_BUTTON_WIDTH = (int) dp2px(120);
        LIKE_BUTTON_HEIGHT = (int) dp2px(45);
        EPISODE_BUTTON_SIZE = (int) dp2px(40);

        like.setOnClickListener(clickListener);
        like.setOnFocusChangeListener(changeListener);

        for (int i = 0; i < episodeListView.getChildCount(); i++) {
            Button ep = (Button) episodeListView.getChildAt(i);
            createEpisodeButton(ep);
        }
        return v;
    }

}