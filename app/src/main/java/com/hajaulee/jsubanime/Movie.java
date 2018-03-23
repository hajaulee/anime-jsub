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

import java.io.Serializable;
import java.util.Arrays;

/*
 * Movie class represents video entity with title, description, image thumbs and video url.
 */
public class Movie implements Serializable {
    static final long serialVersionUID = 727566175075960653L;
    public static final String LOAD_DESCRIPTION_MESSAGE = "Đang tải tóm tắt nội dung";
    private long id;
    private String title;
    private String description;
    private String bgImageUrl;
    private String cardImageUrl;
    private String videoUrl;
    private String studio;
    private String category;
    private String genres;
    private String year;
    private String duration;
    private String firstEpisodeLink;
    private String currentEp;
    private int watchingSecond;
    private int episodeCount;
    private String[] episodeList;

    public Movie() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getBackgroundImageUrl() {
        return bgImageUrl;
    }

    public void setBackgroundImageUrl(String bgImageUrl) {
        this.bgImageUrl = bgImageUrl;
    }

    public String getCardImageUrl() {
        return cardImageUrl;
    }

    public void setCardImageUrl(String cardImageUrl) {
        this.cardImageUrl = cardImageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    @Override
    public String toString() {
        return "Movie{" +
                "\n\t id=" + id +
                ",\n\t title='" + title + '\'' +
                ",\n\t videoUrl='" + videoUrl + '\'' +
                ",\n\t backgroundImageUrl='" + bgImageUrl + '\'' +
                ",\n\t cardImageUrl='" + cardImageUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object m) {
        if (m instanceof Movie)
            return this.getVideoUrl().equals(((Movie) m).getVideoUrl());
        else return false;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getFirstEpisodeLink() {
        return firstEpisodeLink;
    }

    public void setFirstEpisodeLink(String firstEpisodeLink) {
        this.firstEpisodeLink = firstEpisodeLink;
    }

    public String getCurrentEp() {
        return currentEp;
    }

    public void setCurrentEp(String currentEp) {
        this.currentEp = currentEp;
    }

    public String[] getEpisodeList() {
        return episodeList;
    }

    public void setEpisodeList(String[] episodeList) {
        this.episodeList = episodeList;
        setEpisodeCount(episodeList.length - 1);
        if (getCurrentEp() == null)
            setCurrentEp(episodeList[1]);
    }

    public String getNextEp() {
        if (episodeList == null)
            return null;
        int currentIndex = Arrays.asList(episodeList).indexOf(getCurrentEp());
        if (currentIndex == episodeList.length - 1)
            return episodeList[currentIndex];
        return episodeList[currentIndex + 1];
    }

    public String getPreviousEp() {
        if (episodeList == null)
            return null;
        int currentIndex = Arrays.asList(episodeList).indexOf(getCurrentEp());
        if (currentIndex == 1)
            return episodeList[currentIndex];
        return episodeList[currentIndex - 1];
    }

    public int getWatchingSecond() {
        return watchingSecond;
    }

    public void setWatchingSecond(int watchingSecond) {
        this.watchingSecond = watchingSecond;
    }

    public int getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        this.episodeCount = episodeCount;
    }
}
