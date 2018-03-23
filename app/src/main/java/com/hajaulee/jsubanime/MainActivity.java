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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/*
 * MainActivity class that loads {@link MainFragment}.
 */
public class MainActivity extends Activity {

    public static final String UPDATE_LINK = "http://12a1.wc.lt/apk/jsubanime/app-release.app";
    public static final String INFO_OF_UPDATE = "http://12a1.wc.lt/apk/jsubanime/version.txt";
    public static final double APP_VERSION = 20180317.4;
    private static Activity activity;
    public static final int REQUEST = 1997;
    private boolean BACK_CLICKED = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getPermissions();

        activity = this;

        setContentView(R.layout.activity_main);
    }

    public static Activity getInstance() {
        return activity;
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        if (BACK_CLICKED) {
//            this.finish();
//        } else {
//            Toast.makeText(this, "Nhấn nút quay lại lần nữa để thoát", Toast.LENGTH_SHORT).show();
//        }
//        BACK_CLICKED ^= true;
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        BACK_CLICKED = false;
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
        final ProgressDialog dialog = ProgressDialog.show(this, "Kiểm tra cập nhật",
                "Đang tìm kiếm các bản cập nhật", true);
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
        mProgressDialog.setMessage("Đang tải bản cập nhật");
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
        UpToDateDialog.setTitle("Không tìm thấy bản cập nhật");
        UpToDateDialog.setMessage("Phiên bản hiện tại là mới nhất");
        UpToDateDialog.setPositiveButton("Quay lại", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        UpToDateDialog.create().show();
    }

    public void showUpdateConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tìm thấy bản cập nhật mới.");
        builder.setMessage("Bạn có muốn cập nhật không?");
        builder.setCancelable(false);
        builder.setPositiveButton("Để sau", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, "Hãy cập nhật bản mới để có nhiều chức năng tuyệt vời.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                startUpdate();
            }
        });
        builder.create().show();

    }
}
