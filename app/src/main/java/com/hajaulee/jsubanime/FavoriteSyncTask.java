package com.hajaulee.jsubanime;

import android.os.AsyncTask;
import android.widget.Toast;

public class FavoriteSyncTask extends AsyncTask<String, String, Boolean> {
    private String user;
    private String password;
    static final String SYNC_SERVER = "";
    @Override
    protected Boolean doInBackground(String... strings) {
        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        Toast.makeText(MainActivity.getInstance(), 
                success?MainActivity.getStringR(R.string.sync_success):MainActivity.getStringR(R.string.sync_fail), 
                Toast.LENGTH_SHORT  ).show();
    }
}
