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
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

/*
 * MainActivity class that loads {@link MainFragment}.
 */
public class MainActivity extends Activity {

    public static final String UPDATE_LINK = "http://12a1.wc.lt/apk/jsubanime/app-release.app";
    public static final String INFO_OF_UPDATE = "http://12a1.wc.lt/apk/jsubanime/version.txt";
    public static final double APP_VERSION = 20180412.3;
    private static Activity activity;
    public static final int REQUEST = 1997;
    public static final String WHAT_NEW =
                    "20180406.0: Fix lỗi url, cải thiện hiệu suất.\n" +
                    "20180409.0: Cải thiện hiệu suất chuyển tập.\n" +
                    "20180412.1: Thêm chức năng xem từ phút bỏ dở.\n" +
                    "20180412.2: Bổ sung thêm ngôn ngữ.\n" +
                    "20180412.3: Thêm màn hình đợi.\n";
    private Dialog helloDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getPermissions();

        activity = this;
        setContentView(R.layout.activity_main);

        showHelloDialog();
    }

    public static Activity getInstance() {
        return activity;
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
                    Toast.makeText(this, "Hãy cấp quyền ghi bộ nhớ để ứng dụng có thể cập nhật.", Toast.LENGTH_LONG).show();
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
        conf.locale = new Locale("ja");
        DisplayMetrics metrics = new DisplayMetrics();
        getInstance().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Resources resources = new Resources(getInstance().getAssets(), metrics, conf);
        String str = resources.getString(id);
        return str;
    }

    public void showHelloDialog() {

        helloDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        helloDialog.setCancelable(false);
        helloDialog.addContentView(((LayoutInflater) MainActivity
                .getInstance()
                .getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_hello, null),
                new WindowManager.LayoutParams(WindowManager.LayoutParams.FLAG_FULLSCREEN));
        helloDialog.show();
    }

    public void hideHelloDialog(){
        helloDialog.hide();
        helloDialog.dismiss();
    }
    public void showSettingsDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) MainActivity
                .getInstance()
                .getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.layout_settings, null);
        Spinner spinner = (Spinner) v.findViewById(R.id.language_selector);
        String languages[] = {"Tiếng Việt", "日本語"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        b.setView(v);
        b.create().show();
    }

    public static void showWatchInMiddleConfirmationDialog(final VideoDetailsFragment sender, final Intent intent, final Movie m) {
        AlertDialog.Builder builder = new AlertDialog.Builder(sender.getActivity());
        builder.setTitle("Bạn có muốn xem tiếp tại phần bỏ dở không?");
        builder.setMessage("Bạn đang xem tập " + m.getCurrentEp() + " tại " +
                VideoDetailsFragment.timeFormat(m.getWatchingSecond()));
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
