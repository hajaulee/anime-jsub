package com.hajaulee.jsubanime;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.NetworkOnMainThreadException;
import android.text.Html;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by HaJaU on 26-02-18.
 */

public class LoadAnjsubData {
    Fragment fragSender;
    Context context;

    public enum LOAD_ANIME_TASK {
        LOAD_MOVIE_INFO,
        LOAD_DESCRIPTION,
        LOAD_VIETNAMESE_DESCRIPTION_AND_BG_IMAGE
    }

    public LoadAnjsubData(Fragment sender) {
        this.fragSender = sender;
    }

    public LoadAnjsubData(Context sender) {
        this.context = sender;
    }

    public void load() {
        new LoadWebContentTask(fragSender, 0).execute("https://www.anjsub.com/");
        new LoadWebContentTask(fragSender, 1).execute("https://www.anjsub.com/search/label/Comedy");
        new LoadWebContentTask(fragSender, 2).execute("https://www.anjsub.com/search/label/Sci-Fi");
    }

    public void load(int i) {
        int lastMovieInRow = ((MainFragment) fragSender).totalMovieList.get(i).size();
        switch (i) {
            case 0:
                new LoadWebContentTask(fragSender, 0, lastMovieInRow).execute("http://www.anjsub.com/");
                break;
            case 1:
                new LoadWebContentTask(fragSender, 1, lastMovieInRow).execute("http://www.anjsub.com/search/label/Comedy");
                break;
            case 2:
                new LoadWebContentTask(fragSender, 2, lastMovieInRow).execute("http://www.anjsub.com/search/label/Sci-Fi");
                break;
        }
    }

    public void load(int i, String url) {
        int lastMovieInRow = 0;
        if (fragSender instanceof MainFragment) {
            lastMovieInRow = ((MainFragment) fragSender).totalMovieList.get(i).size();
        }
        new LoadWebContentTask(fragSender, i, lastMovieInRow).execute(url);
    }


    class LoadWebContentTask extends AsyncTask<String, Void, String> {

        private static final String separatorChar = "-hajau-";
        StringBuffer webContent = new StringBuffer();
        String URL;
        Context context;
        Fragment fragSender;
        int indexInTotalMovieList;
        int lastItemInRow = 0;
        Movie movieNeedDescription;
        LOAD_ANIME_TASK task;

        public LoadWebContentTask(Fragment sender, int i) {
            indexInTotalMovieList = i;
            this.fragSender = sender;
        }

        public LoadWebContentTask(Fragment sender, int i, int j) {
            this(sender, i);
            lastItemInRow = j;
        }

        public LoadWebContentTask(Movie movie, LOAD_ANIME_TASK task) {
            this.movieNeedDescription = movie;
            this.task = task;
        }

        public LoadWebContentTask(Context sender) {
            this.context = sender;
        }

        protected String doInBackground(String... urls) {
            URL = urls[0];
            try {
                Log.d("LoadAnjsub:", "START");
                URL web = new URL(URL);
                System.setProperty("http.agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)" +
                                " Chrome/64.0.3282.186 Safari/537.36");
                System.setProperty("http.keepAlive", "false");
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                web.openStream()));
                Log.d("LoadAnjsub:", "LOADED");
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    webContent.append(inputLine);
                }
                Log.d("LoadAnjsub:", "COMPLETE");
                in.close();
                Log.d("LoadAnjsub:", "CLOSE");
            } catch (MalformedURLException e) {
                Log.d("LoadAnjsubDataError:", "MalformedURLException: " + e.toString());
            } catch (IOException e) {
                Log.d("LoadAnjsubDataError:", "IOException: " + e.toString());
            } catch (NetworkOnMainThreadException e) {
                Log.d("LoadAnjsubDataError:", "NetworkOnMainThreadException: " + e.toString());
            }

            return webContent.toString();
        }

        @Override
        protected void onPostExecute(String mwebContent) {
            // TODO: check this.exception
            // TODO: do something with the feed
            Log.d("LoadAnjsub:", "DONE");
            if (task == LOAD_ANIME_TASK.LOAD_DESCRIPTION) {//Load description
                int summaryStart = webContent.indexOf("Summary:");
                int summaryEnd = webContent.indexOf("<h1", summaryStart + 1);
                if (summaryStart == -1 || summaryEnd == -1)
                    return;
                LogThis(summaryStart + "|" + summaryEnd);
                String descriptionArea[] = webContent.substring(summaryStart, summaryEnd)
                        .replaceAll("\\<.*?>", separatorChar)
                        .replaceAll("(" + separatorChar + ")+", separatorChar)
                        .split(separatorChar);
                int h2Tag = webContent.indexOf("<h2", summaryEnd);
                int bTag = webContent.substring(0, h2Tag).lastIndexOf("<b");
                int bTagClose = webContent.indexOf("</b", bTag + 1);
                String animeName = Html.fromHtml(webContent.substring(bTag, bTagClose)).toString();

                bTag = webContent.indexOf("<b>", h2Tag);
                bTagClose = webContent.indexOf("</b>", bTag);
                String year = Html.fromHtml(webContent.substring(bTag, bTagClose)).toString();

                bTag = webContent.indexOf("<b>", bTag + 1);
                bTagClose = webContent.indexOf("</b>", bTag);
                String genres = Html.fromHtml(webContent.substring(bTag, bTagClose)).toString();

                bTag = webContent.indexOf("<b>", bTag + 1);
                bTagClose = webContent.indexOf("</b>", bTag);
                String duration = Html.fromHtml(webContent.substring(bTag, bTagClose)).toString();

                int watchLink = webContent.indexOf("href", bTagClose);
                watchLink = webContent.indexOf("\"", watchLink);
                int endWatchLink = webContent.indexOf("\"", watchLink + 1);
                String link = webContent.substring(watchLink + 1, endWatchLink);

                movieNeedDescription.setYear(year);
                movieNeedDescription.setGenres(genres);
                movieNeedDescription.setDuration(duration);
                movieNeedDescription.setFirstEpisodeLink(link);

                String description = animeName + "\n<br><b>Tóm tắt:</b> ";
                for (int j = 1; j < descriptionArea.length; j++)
                    description += descriptionArea[j] + "\n";
                movieNeedDescription.setDescription(description);
                try {
                    new LoadWebContentTask(movieNeedDescription,
                            LOAD_ANIME_TASK.LOAD_VIETNAMESE_DESCRIPTION_AND_BG_IMAGE)
                            .execute("http://www.anivn.com/tim-kiem/" +
                                    URLEncoder.encode(movieNeedDescription.getTitle(), "utf-8") +
                                    ".html");
                } catch (UnsupportedEncodingException e) {
                    LogThis(e.toString());
                }

            } else if (task == LOAD_ANIME_TASK.LOAD_VIETNAMESE_DESCRIPTION_AND_BG_IMAGE) { // load description from anivn.com
                if (URL.contains("tim-kiem")) { // Load search result page
                    LogThis("\n\n\n\n" + "Search for: " + movieNeedDescription.getTitle() + "\n\n\n\n");
                    //toFile(movieNeedDescription.getTitle(), mwebContent);
                    LogThis("Load search result page");
                    List<String[]> searchResults = new ArrayList<>();
                    int widgetPostOne = 0;
                    for (int i = 0; i < 20; i++) {
                        widgetPostOne = webContent.indexOf("widget-post", widgetPostOne + 1);
                        LogThis("widget-post: " + widgetPostOne);
                        if (widgetPostOne == -1)
                            break;
                        int start = webContent.indexOf("<a", widgetPostOne);
                        start = webContent.indexOf("\"", start + 1);
                        int end = webContent.indexOf("\"", start + 1);
                        String animeUrl = webContent.substring(start + 1, end);
                        start = webContent.indexOf("\"", end + 1);
                        end = webContent.indexOf("\"", start + 1);
                        String animeName = webContent.substring(start + 1, end);
                        LogThis(animeName + "|" + animeUrl);
                        searchResults.add(new String[]{animeName, animeUrl});
                    }
                    if (searchResults.isEmpty()) { //Anime not found in anivn.com

                        LogThis("Anime not found in anivn.com");
                        LogThis(URL);
                    } else {//Have a list of search result
                        int i = 0;
                        LogThis("Have a list of search result");
                        for (; i < searchResults.size(); ++i) {
                            if (searchResults.get(i)[0].equals(movieNeedDescription.getTitle())) {
                                new LoadWebContentTask(movieNeedDescription,
                                        LOAD_ANIME_TASK.LOAD_VIETNAMESE_DESCRIPTION_AND_BG_IMAGE)
                                        .execute(searchResults.get(i)[1]);
                                LogThis("Load:" + searchResults.get(i)[1]);
                                break;
                            } else {
                                LogThis("[" + searchResults.get(i)[0] + "|" + movieNeedDescription.getTitle() + "]");
                            }
                        }
                        if (i == searchResults.size()) { // Search result not contain the anime which want to add description
                            LogThis(movieNeedDescription.getTitle() + ": Search result not contain the anime which want to add description");
                        }
                    }
                    LogThis("\n\n\n\n" + "End Search for: " + movieNeedDescription.getTitle() + "\n\n\n\n");
                } else { // Load info page

                    //toFile(movieNeedDescription.getTitle() + "-INFO", mwebContent);
                    LogThis("Load info page");
                    int descriptionStart = webContent.indexOf("description-clip\"");
                    descriptionStart = webContent.indexOf("message", descriptionStart);
                    descriptionStart = webContent.indexOf(">", descriptionStart);
                    int descriptionEnd = webContent.indexOf("ads-longvan", descriptionStart);
                    int img = webContent.indexOf("<img", descriptionStart);
                    String animeName = movieNeedDescription.getDescription().split("\n")[0];
                    if (img > descriptionStart && img < descriptionEnd) {//Have bg image
                        String description = Html.fromHtml(webContent.substring(descriptionStart + 1, img)).toString();
                        int start = webContent.indexOf("\"", img);
                        int end = webContent.indexOf("\"", start + 1);
                        String bgImage = webContent.substring(start + 1, end);
                        movieNeedDescription.setDescription(animeName + "\n<br><b>Tóm tắt:</b> " + description);
                        if (URLUtil.isValidUrl(bgImage))
                            movieNeedDescription.setBackgroundImageUrl(bgImage);
                    } else {
                        String description = Html.fromHtml(webContent.substring(descriptionStart + 1, descriptionEnd - 12))
                                .toString();
                        movieNeedDescription.setDescription(animeName + "\n<br><b>Tóm tắt:</b> " + description);
                    }
                }

            } else { // Load Movie list
                LogThis("Load Movie list");
                try {
                    final List<Movie> movieList = loadMovieList();

                    if (indexInTotalMovieList == SearchFragment.TEST_SEARCH) {// Test serach
                        // Do nothing
                        //This is preparing for connection
                        Toast.makeText(fragSender.getActivity(), "Prepare", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (indexInTotalMovieList == SearchFragment.INDEX) { // Load search result
                        Log.d("Search::", "End seach");
                        SearchFragment.searchResult = movieList;
                        if (fragSender instanceof SearchFragment) {
                                    if (movieList.isEmpty()) {
                                        Toast.makeText(fragSender.getActivity(),
                                                "Không tìm thấy kết quả", Toast.LENGTH_SHORT).show();
                                    } else {
                                        ((SearchFragment) fragSender).setSearchResultAdapter(movieList);
                                    }
                        }
                        return;
                    }
                    final MainFragment fragment = (MainFragment) fragSender;
                    try {
                        fragment.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                fragment.totalMovieList.set(indexInTotalMovieList, movieList);
                                if (MovieList.allLoaded(fragment.totalMovieList))
                                    fragment.loadRows(indexInTotalMovieList, lastItemInRow);
                            }
                        });
                    } catch (NullPointerException e) {
                        //To do something
                    }
                    for (Movie movie : movieList) {
                        LogThis(movie.toString());
                    }
                } catch (UnsupportedEncodingException e) {
                    LogThis(e.toString());
                }
            }
        }

        private void toFile(String fName, String str) {
            String folderName = "Anjsub";
            String fileFormat = ".html";
            String folderUri = Environment.getExternalStorageDirectory() + File.separator + folderName;
            File folder = new File(folderUri);
            boolean success;
            if (!folder.exists())
                success = folder.mkdir();
            else
                success = true;
            if (!success) {
                LogThis("Can not create Folder");
                return;
            }

            try {
                FileOutputStream is = new FileOutputStream(folderUri + File.separator + fName + fileFormat);
                is.write(str.getBytes());
                is.close();
            } catch (Exception e) {
                Log.d("Error write: " + fName, e.toString());
            }
        }

        private String getThumbnailLink(String s) {
            int start = s.indexOf("\"");
            int end = s.indexOf("\"", start + 1);
            return start == -1 ? "" : s.substring(start + 1, end).replace("s72-c", "s640");
        }

        private void LogThis(String s) {
            Log.d("LoadAnjsub::", s);
        }

        private String getCategory(String[] movieBlockComponent) {
            String buffer = "";
            for (int index = 5; index < movieBlockComponent.length; ++index) {
                buffer += movieBlockComponent[index] + ", ";
            }
            return buffer.substring(0, buffer.length() - 2);
        }

        private String getURL(String block) {
            try {
                int aTagStart = block.indexOf("<a", 1);
                int linkStart = block.indexOf("'", aTagStart + 1);
                int linkEnd = block.indexOf("'", linkStart + 1);
                return block.substring(linkStart + 1, linkEnd);
            } catch (Exception e) {
                LogThis(block + e.toString());
                return "";
            }
        }

        protected List<Movie> loadMovieList() throws UnsupportedEncodingException {
            List<Movie> list = null;
            if (fragSender instanceof MainFragment)
                list = ((MainFragment) fragSender).totalMovieList.get(indexInTotalMovieList);
            if (list == null)
                list = new ArrayList<>();
            int openTagOfBlock = 0;
            int closeTagOfBlock = 0;
            int openWaktuClass = 0;
            int closeWaktuClass = 0;
            LogThis("Create movie list");
            for (int index = 0; index < 18; ++index) {

                openTagOfBlock = webContent.indexOf("<article", closeTagOfBlock + 1);
                closeTagOfBlock = webContent.indexOf("</article", openTagOfBlock + 1);

                openWaktuClass = webContent.indexOf("'waktu'", closeWaktuClass + 1);
                closeWaktuClass = webContent.indexOf("</span>", openWaktuClass + 1);
                if (openTagOfBlock == -1 || closeTagOfBlock == -1) {
                    LogThis("Block Not found");
                    LogThis(webContent.toString());
                    break;
                }else
                    LogThis("" + index);
                String updateTime = webContent
                        .substring(openWaktuClass + 8, closeWaktuClass)
                        .replaceAll("\\<.*?>", "");
                DateFormat df = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
                DateFormat ndf = new SimpleDateFormat("yyyy-MM-dd");
                Date d;
                try {
                    d = df.parse(updateTime);
                    MovieList.updateMax[indexInTotalMovieList] = ndf.format(d);
                    LogThis("Time:" + ndf.format(d));
                } catch (Exception e) {
                    LogThis("Invalid time: \"" + updateTime + "\"" + e.toString());
                }
                String movieBlock = webContent.substring(openTagOfBlock, closeTagOfBlock);
                String movieUrl = getURL(movieBlock);
                movieBlock = movieBlock.replaceAll("\\<.*?>", separatorChar)
                        .replaceAll("(" + separatorChar + ")+", separatorChar);
                LogThis(movieBlock);
                String[] movieBlockComponent = movieBlock.split(separatorChar);
                Movie movie = MovieList.buildMovieInfo("category",
                        movieBlockComponent[2], //Title
                        Movie.LOAD_DESCRIPTION_MESSAGE,
                        getCategory(movieBlockComponent), // Real is studio
                        movieUrl,
                        getThumbnailLink(movieBlockComponent[3]), // Bg Image
                        getThumbnailLink(movieBlockComponent[3])); // Card image

                new LoadWebContentTask(movie, LOAD_ANIME_TASK.LOAD_DESCRIPTION)
                        .execute(movie.getVideoUrl());
                list.add(movie);
                LogThis(movie.toString());
                MovieList.totalMovieList.add(movie);
            }
            return list;
        }
    }
}
