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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/*
 * MainActivity class that loads {@link MainFragment}.
 */
public class MainActivity extends Activity {

    private static final String UPDATE_LINK = "http://12a1.wc.lt/apk/jsubanime/app-release.app";
    private static final String INFO_OF_UPDATE = "http://12a1.wc.lt/apk/jsubanime/version.txt";
    private static final String LANGUAGE_CODE_j = "ja";
    private static final String LANGUAGE_CODE_v = "vi";
    private static final String VIETNAMESE = "Tiếng Việt";
    private static final String JAPANESE = "日本語";
    private static final String ON = "On";
    private static final String OFF = "Off";
    private static final String SETTING_FILE = "settings.fcd";
    private static final double APP_VERSION = 20180527.0;
    private static Activity activity;
    private static String LANGUAGE_CODE = LANGUAGE_CODE_v;
    private static String SYNC_STATUS = ON;
    private static final int REQUEST = 1997;
    private static final String WHAT_NEW =
            "20180406.0: Sửa lỗi đường dẫn video, cải thiện hiệu suất.\n" +
                    "20180409.0: Cải thiện hiệu suất chuyển tập.\n" +
                    "20180412.1: Thêm chức năng xem từ phút bỏ dở.\n" +
                    "20180412.2: Bổ sung thêm ngôn ngữ.\n" +
                    "20180412.3: Thêm màn hình đợi.\n" +
                    "20180415.0: Sửa lỗi.\n" +
                    "20180420.0: Sửa lỗi.\n" +
                    "20180425.0: Sửa lỗi tải danh sách tập.\n" +
                    "20180527.0: Sửa lỗi về ngôn ngữ.";
    private Dialog helloDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getPermissions();

        activity = this;

        String settings = MovieList.readFile(SETTING_FILE);
        if (settings == null) {
            LANGUAGE_CODE = LANGUAGE_CODE_v;
            SYNC_STATUS = ON;
            saveSetting();
        } else {
            getSetting(settings);
        }


        setContentView(R.layout.activity_main);

        showHelloDialog();
    }

    public static Activity getInstance() {
        return activity;
    }

    public void saveSetting() {
        MovieList.saveFile(SETTING_FILE, LANGUAGE_CODE + " " + SYNC_STATUS);
    }

    public void getSetting(String settings) {
        String[] arrayOfSetting = settings.split(" ");
        LANGUAGE_CODE = arrayOfSetting[0];
        SYNC_STATUS = arrayOfSetting[1];
        // Toast.makeText(getInstance(), settings, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.finish();
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, SearchActivity.class));
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do here
                } else {
                    Toast.makeText(this, getStringR(R.string.permission_request), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void getPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions((Activity) this, PERMISSIONS, REQUEST);
            } else {
                //do here
            }
        } else {
            //do here
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void showCheckUpdateDialog() {
        final ProgressDialog dialog = ProgressDialog.show(this, getStringR(R.string.check_update),
                getStringR(R.string.find_update), true);
        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... strings) {
                String URL = strings[0];
                StringBuilder webContent = new StringBuilder();
                try {
                    URL web = new URL(URL);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    web.openStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        //System.out.println(inputLine);
                        webContent.append(inputLine);
                    }
                } catch (Exception e) {
                    Log.d("Tasss", e.toString());
                }
                return webContent.toString();
            }

            @Override
            public void onPostExecute(String result) {
                dialog.dismiss();
                double newVersion = Double.parseDouble(result);
                if (newVersion > APP_VERSION) {
                    showUpdateConfirmationDialog();
                } else {
                    showUpToDateDialog();
                }
            }
        }.execute(INFO_OF_UPDATE);

    }

    public void startUpdate() {

        // instantiate it within the onCreate method
        ProgressDialog mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getStringR(R.string.updating));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);

        // execute this when the downloader must be fired
        final DownloadTask downloadTask = new DownloadTask(this, mProgressDialog);
        downloadTask.execute(UPDATE_LINK);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
    }

    public void showUpToDateDialog() {
        AlertDialog.Builder UpToDateDialog = new AlertDialog.Builder(this);
        UpToDateDialog.setTitle(getStringR(R.string.update_not_found));
        UpToDateDialog.setMessage(getStringR(R.string.up_to_date));
        UpToDateDialog.setPositiveButton(getStringR(R.string.back), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        UpToDateDialog.create().show();
    }

    public void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getStringR(R.string.updating_history));
        builder.setMessage(WHAT_NEW);
        builder.setCancelable(true);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    public void showUpdateConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getStringR(R.string.update_found));
        builder.setMessage(getStringR(R.string.want_to_update));
        builder.setCancelable(false);
        builder.setPositiveButton(getStringR(R.string.after), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, getStringR(R.string.update_to_great), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(getStringR(R.string.update), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                startUpdate();
            }
        });
        builder.create().show();
    }

    public static String getStringR(int id) {
        Configuration conf = getInstance().getResources().getConfiguration();
        conf.locale = new Locale(LANGUAGE_CODE);
        DisplayMetrics metrics = new DisplayMetrics();
        getInstance().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Resources resources = new Resources(getInstance().getAssets(), metrics, conf);
        return resources.getString(id);
    }

    public void showHelloDialog() {

        helloDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        helloDialog.setCancelable(false);
        helloDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)
                    MainActivity.this.finish();
                return false;
            }
        });
        helloDialog.addContentView(((LayoutInflater) MainActivity
                        .getInstance()
                        .getBaseContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_hello, null),
                new WindowManager.LayoutParams(WindowManager.LayoutParams.FLAG_FULLSCREEN));
        helloDialog.show();
    }

    public void hideHelloDialog() {
        helloDialog.hide();
        helloDialog.dismiss();
    }

    public Map<String, String> createMap(String title, String subtitle) {
        Map<String, String> dataRow = new HashMap<>(2);
        dataRow.put("title", title);
        dataRow.put("content", subtitle);
        return dataRow;
    }

    public void showRestartConfirmDialog(final int selected) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.restart);
        builder.setMessage(R.string.restart_confirm);
        builder.setNegativeButton(getStringR(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (selected) {
                    case 0:
                        LANGUAGE_CODE = LANGUAGE_CODE_v;
                        break;
                    case 1:
                        LANGUAGE_CODE = LANGUAGE_CODE_j;
                        break;
                }
                saveSetting();
                Intent intent = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
        builder.setPositiveButton(getStringR(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                builder.show().dismiss();
            }
        });
        builder.show();
    }

    public static boolean isVietnamese() {
        return LANGUAGE_CODE.equals(LANGUAGE_CODE_v);
    }

    public void showSettingsDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) MainActivity
                .getInstance()
                .getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.layout_settings, null);
        final List<Map<String, String>> data = new ArrayList<>();
        data.add(createMap(getStringR(R.string.language), LANGUAGE_CODE.equals(LANGUAGE_CODE_v) ? VIETNAMESE : JAPANESE));
        data.add(createMap(getStringR(R.string.sync), SYNC_STATUS.equals(ON) ? ON : OFF));

        final SimpleAdapter settingAdapter = new SimpleAdapter(this,
                data,
                android.R.layout.simple_list_item_2,
                new String[]{"title", "content"},
                new int[]{android.R.id.text1, android.R.id.text2});
        final ListView listView = (ListView) v.findViewById(R.id.listView);
        listView.setAdapter(settingAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        v.findViewById(R.id.languageSetting).setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        v.findViewById(R.id.syncSetting).setVisibility(View.VISIBLE);
                        break;
                }
                listView.setVisibility(View.GONE);
            }
        });

        String languages[] = {VIETNAMESE, JAPANESE};
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, languages);
        ListView languageSetting = (ListView) v.findViewById(R.id.languageSetting);
        languageSetting.setAdapter(languageAdapter);
        languageSetting.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showRestartConfirmDialog(i);
            }
        });

        String options[] = {ON, OFF};
        ArrayAdapter<String> onOffAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options);
        final ListView syncSetting = (ListView) v.findViewById(R.id.syncSetting);
        syncSetting.setAdapter(onOffAdapter);
        syncSetting.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        SYNC_STATUS = ON;
                        break;
                    case 1:
                        SYNC_STATUS = OFF;
                        break;
                }

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        data.remove(1);
                        data.add(1, createMap(getStringR(R.string.sync), SYNC_STATUS.equals(ON) ? ON : OFF));
                        settingAdapter.notifyDataSetChanged();
                    }
                });
                saveSetting();
                syncSetting.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }
        });

        b.setView(v);
        b.show();
    }

    public static void showWatchInMiddleConfirmationDialog(final VideoDetailsFragment sender, final Intent intent, final Movie m) {
        AlertDialog.Builder builder = new AlertDialog.Builder(sender.getActivity());
        builder.setTitle(getStringR(R.string.continue_watching));
        builder.setMessage(String.format(
                getStringR(R.string.current_ep), m.getCurrentEp(),
                VideoDetailsFragment.timeFormat(m.getWatchingSecond())
        ));
        builder.setCancelable(false);
        builder.setNegativeButton(getStringR(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                intent.putExtra(DetailsActivity.WATCH_TIME, m.getWatchingSecond());
                sender.startActivity(intent);
            }
        });
        builder.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                sender.startActivity(intent);
            }
        });
        builder.create().show();
    }
}
