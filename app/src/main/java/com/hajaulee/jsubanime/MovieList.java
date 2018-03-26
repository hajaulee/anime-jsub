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

import android.app.Activity;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ListRow;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public final class MovieList {
    public enum SaveAction{
        ADD,
        REMOVE
    }

    public static String[] updateMax = new String[]{"", "", "", ""};
    public static List<Movie> favoriteMovies;
    public static List<Movie> totalMovieList = new ArrayList<>();
    public static final String FAVORITE_LIST = "FAVORITE_LIST";
    public static final String MOVIE_CATEGORY[] = {
            "Mới nhất",
            "Hài hước",
            "Siêu nhiên",
            "Yêu thích"
    };

    private static List<Movie> list;
    private static long count = 0;

    public static List<Movie> getList() {
        if (list == null) {
            list = setupMovies();
        }
        return list;
    }

    public static boolean isLiked(Movie movie) {
        if (favoriteMovies == null)
            return false;

        return favoriteMovies.contains(movie);
    }

    public static void addFavorite(Movie movie) {
        if (favoriteMovies != null) {
            favoriteMovies.add(movie);
            saveFavoriteMovieList(movie, SaveAction.ADD);
        }
    }

    public static Movie getMovieFromFavoriteList(Movie movie){
        return getMovieFromList(favoriteMovies, movie);
    }

    public static Movie getMovieFromTotalList(Movie movie){
        return getMovieFromList(totalMovieList, movie);
    }

    public static Movie getMovieFromList(List<Movie> list, Movie movie){
        int i = list.indexOf(movie);
        Log.d("Hashzzz", movie.hashCode() +"<<" + movie.getTitle());
        if (i == -1)
            return movie;
        else {
            movie = list.get(i);
            Log.d("Hashzzz", movie.hashCode() +"<<" + movie.getTitle());
            return movie;
        }
    }
    public static void removeFavorite(Movie movie) {
        if (favoriteMovies != null) {
            favoriteMovies.remove(movie);
            saveFavoriteMovieList(movie, SaveAction.REMOVE);
        }
    }

    public static void saveFavoriteMovieList(Movie movie, SaveAction saveAction){
        try {
            FileOutputStream favoriteData = new FileOutputStream(
                    MainActivity.getInstance().getApplicationInfo().dataDir + File.separatorChar + MovieList.FAVORITE_LIST);
            ObjectOutputStream oos = new ObjectOutputStream(favoriteData);
            oos.writeObject(MovieList.favoriteMovies);
            oos.close();


            ArrayObjectAdapter favoriteAdapter = MainFragment.getFavoriteAdapter();
            if (saveAction == SaveAction.ADD){
                favoriteAdapter.add(movie);
            }else if(saveAction == SaveAction.REMOVE) {
                favoriteAdapter.remove(movie);
            }
            favoriteAdapter.notify();
        } catch (Exception e1) {
            Log.d(FAVORITE_LIST, e1.toString());
        }
    }
    public static boolean allLoaded(List<List<Movie>> list){
        if (list == null)
            return false;
        for (int i = 0; i < MainFragment.NUM_ROWS; ++i)
            if (list.get(i) == null)
                return false;
        return true;
    }
    public static void loadFavoriteMovieList(MainFragment mf){
        try {
            FileInputStream favoriteData = new FileInputStream(
                    MainActivity.getInstance().getApplicationInfo().dataDir + File.separatorChar + MovieList.FAVORITE_LIST);
            ObjectInputStream ois = new ObjectInputStream(favoriteData);
            MovieList.favoriteMovies = (List<Movie>)ois.readObject();
            mf.totalMovieList.set(MainFragment.NUM_ROWS - 1, MovieList.favoriteMovies);
            ois.close();
        } catch (Exception e1) {
            favoriteMovies = new ArrayList<>();
            saveFavoriteMovieList(null, SaveAction.ADD);
            Log.d(FAVORITE_LIST, e1.toString());
        }
    }

    public static List<Movie> setupMovies() {
        list = new ArrayList<>();
        String title[] = {
                "Zeitgeist 2010_ Year in Review",
                "Google Demo Slam_ 20ft Search",
                "Introducing Gmail Blue",
                "Introducing Google Fiber to the Pole",
                "Introducing Google Nose"
        };

        String description = "Fusce id nisi turpis. Praesent viverra bibendum semper. "
                + "Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est "
                + "quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit "
                + "amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit "
                + "facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id "
                + "lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.";
        String studio[] = {
                "Studio Zero", "Studio One", "Studio Two", "Studio Three", "Studio Four"
        };
        String videoUrl[] = {
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review.mp4",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search.mp4",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue.mp4",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole.mp4",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose.mp4"
        };
        String bgImageUrl[] = {
//                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/bg.jpg",
                "https://2.bp.blogspot.com/-Vi1_5ioq644/WpF-kwCNtXI/AAAAAAAAGmo/5O2IRgJwzcEWMd8kigEVYlXSsgCHM2P5QCLcBGAs/s640/FAD.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/bg.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue/bg.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/bg.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose/bg.jpg",
        };
        String cardImageUrl[] = {
//                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/card.jpg",
                "https://2.bp.blogspot.com/-Vi1_5ioq644/WpF-kwCNtXI/AAAAAAAAGmo/5O2IRgJwzcEWMd8kigEVYlXSsgCHM2P5QCLcBGAs/s640/FAD.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/card.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue/card.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/card.jpg",
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose/card.jpg"
        };

        for (int index = 0; index < title.length; ++index) {
            list.add(
                    buildMovieInfo(
                            "category",
                            title[index],
                            description,
                            studio[index],
                            videoUrl[index],
                            cardImageUrl[index],
                            bgImageUrl[index]));
        }

        return list;
    }

    public static Movie buildMovieInfo(String category, String title,
                                       String description, String genres, String videoUrl, String cardImageUrl,
                                       String backgroundImageUrl) {
        Movie movie = new Movie();
        movie.setId(count++);
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setGenres(genres);
        movie.setCategory(category);
        movie.setCardImageUrl(cardImageUrl);
        movie.setBackgroundImageUrl(backgroundImageUrl);
        movie.setVideoUrl(videoUrl);
        return movie;
    }
}