package io.abhishekpareek.app.MyGistHub.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.abhishekpareek.app.MyGistHub.R;
import io.abhishekpareek.app.MyGistHub.models.MyGist;
import io.abhishekpareek.app.MyGistHub.utils.Connectivity;

public class UpdateGistActivity extends AppCompatActivity {
    private EditText description_et;
    private EditText filename_et;
    private EditText contents_et;
    private TextView gistCreatedAt_tv;
    private ProgressBar mProgressBar;
    private TextView fetch_gist_tv;
    private LinearLayout edit_gist_ll;

    private String gistId;
    private Boolean gist_starred = false;
    private Boolean gist_public = false;


    private String TAG = this.getClass().getSimpleName();

    private GistService service;
    private Gist mGist;
    private Snackbar mSnackbar;
    private CoordinatorLayout coordinatorLayout;
    private RadioButton public_rb, private_rb;
    private CheckBox star_cb;
    private boolean mStarGist;

    private void init_snackbar(String s) {
        mSnackbar = Snackbar
                .make(coordinatorLayout, s, Snackbar.LENGTH_INDEFINITE);
        mSnackbar.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gist);

        init_ui();
    }

    private void init_ui() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.update_gist_coordinator);

        description_et = (EditText) findViewById(R.id.description_et);
        filename_et = (EditText) findViewById(R.id.gist_filename_et);
        contents_et = (EditText) findViewById(R.id.contents_et);
        gistCreatedAt_tv = (TextView) findViewById(R.id.createdAt_tv);
        fetch_gist_tv = (TextView) findViewById(R.id.fetch_gist_tv);
        edit_gist_ll = (LinearLayout) findViewById(R.id.edit_gist_ll);
        public_rb = (RadioButton) findViewById(R.id.update_public_rb);
        private_rb = (RadioButton) findViewById(R.id.update_private_rb);
        star_cb = (CheckBox) findViewById(R.id.update_star_cb);

        contents_et.setFocusable(true);
        contents_et.setClickable(true);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(0);

        Bundle b = getIntent().getExtras();
        MyGist myGist = b.getParcelable("myGist");

        SharedPreferences prefs = getSharedPreferences(getResources().getString(R.string.token_str), MODE_PRIVATE);
        String token_str = getResources().getString(R.string.token_str);
        String token = prefs.getString(token_str, null);

        if (token != null) {
            service = new GistService();
            service.getClient().setOAuth2Token(token);

            gistId = myGist.getGistId();
            new GithubGistDownloadTask().execute(service);
        } else {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.gistactivity_menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Capture the back action

        if (!Connectivity.isConnected(this)) {
            init_snackbar("No Internet Found!");
        } else {
            if (item.getItemId() == android.R.id.home) {
                onBackPressed();
                return true;

            } else if (item.getItemId() == R.id.action_update) {
                if (String.valueOf(filename_et.getText()).equals("") || String.valueOf(contents_et.getText()).equals("")) {
                    init_snackbar("Fields can't be blank");
                    return true;
                }

                mProgressBar.setIndeterminate(true);
                mProgressBar.setProgress(0);

                // Save the old gistFile
                Map<String, GistFile> old_gistFile = mGist.getFiles();
                mGist.setDescription(String.valueOf(description_et.getText()));
                //TODO implement gist title and content update
                GistFile file = null;

                Set set = old_gistFile.entrySet();
                // Get an iterator
                Iterator i = set.iterator();
                String title, updatedAt;
                // Display elements
                while(i.hasNext()) {
                    Map.Entry me = (Map.Entry)i.next();
                    file = mGist.getFiles().get(me.getKey());
                }

                file.setContent(String.valueOf(contents_et.getText()));
                file.setFilename(String.valueOf(filename_et.getText()));
                mGist.setFiles(Collections.singletonMap(String.valueOf(filename_et.getText()), file));

                if (public_rb.isChecked())
                    mGist.setPublic(true);
                if (private_rb.isChecked())
                    mGist.setPublic(false);

                if (star_cb.isChecked())
                    mStarGist = true;

                new GithubGistUpdateTask().execute(service);
            } else if (item.getItemId() == R.id.action_delete) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mProgressBar.setIndeterminate(true);
                        mProgressBar.setProgress(0);

                        new GithubGistDeleteTask().execute(service);
                    }
                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.setTitle("This gist will be permanently deleted. Are you sure?");
                dialog.show();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(1);
        finish();
    }

    private class GithubGistDownloadTask extends AsyncTask<Object, Integer, Map<String, GistFile>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(true);
            mProgressBar.setProgress(0);
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Map<String, GistFile> doInBackground(Object... params) {
            Map<String, GistFile> files = null;
            try {
                Gist gist = service.getGist(gistId);
                ////////////////Log.d(TAG, gist.getFiles().toString());
                files = gist.getFiles();
                mGist = gist;

                gist_public = mGist.isPublic();
                gist_starred = service.isStarred(gistId);

            } catch (IOException e) {
                e.printStackTrace();
                init_snackbar(e.toString());
            }
            return files;
        }

        protected void onPostExecute(Map<String, GistFile> result) {
            ////////////////Log.d(TAG, "Done!");

            fetch_gist_tv.setVisibility(View.GONE);
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(100);
            mProgressBar.setVisibility(View.INVISIBLE);

            edit_gist_ll.setVisibility(View.VISIBLE);

            description_et.setText(mGist.getDescription());

            if (gist_public)
                public_rb.setChecked(true);
            else
                private_rb.setChecked(true);

            if (gist_starred)
                star_cb.setChecked(true);


            Set set = mGist.getFiles().entrySet();
            // Get an iterator
            Iterator i = set.iterator();
            String title, updatedAt;
            // Display elements
            while(i.hasNext()) {
                Map.Entry me = (Map.Entry)i.next();
                title = String.valueOf(me.getKey());
                filename_et.setText(title);

                SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/M/y, HH:mm");
                updatedAt = simpleDate.format(mGist.getUpdatedAt());
                gistCreatedAt_tv.setText("Last Updated : " + updatedAt);
            }

            if (result != null && !result.isEmpty()) {
                for (GistFile gistFile : result.values()) {
                    contents_et.setText(gistFile.getContent());
                }
            }

        }
    }

    private class GithubGistUpdateTask extends AsyncTask<Object, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(true);
            mProgressBar.setProgress(0);
        }

        @Override
        protected Integer doInBackground(Object... params) {
            Integer res = 0;
            try {
                ////////////////Log.d(TAG, "foo");
                //service.updateGist(mGist);
                service.starGist(mGist.getId());
                service.updateGist(mGist);
                res = 1;
            } catch (IOException e) {
                e.printStackTrace();
                res = -1;
                init_snackbar(e.toString());
            }

            return res;
        }

        protected void onPostExecute(Integer result) {
            ////////////////Log.d(TAG, "Done!");

            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(100);

            description_et.setSelected(false);
            filename_et.setSelected(false);

            if(result == 1) {
                Toast.makeText(UpdateGistActivity.this, "Gist has been successfully updated!", Toast.LENGTH_LONG).show();

                String updatedGistId = mGist.getId();
                Intent data = new Intent();

                Bundle b = new Bundle();
                b.putString("updated_gist_id", updatedGistId);
                b.putBoolean("starred", mStarGist);
                b.putBoolean("public", gist_public);

                data.putExtras(b);

                setResult(1, data);
                finish();
            }
            else if (result != -1){
                Toast.makeText(UpdateGistActivity.this, "Gist update failed!", Toast.LENGTH_LONG).show();
                init_snackbar("Gist update failed!");
            }

        }
    }

    private class GithubGistDeleteTask extends AsyncTask<Object, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(true);
            mProgressBar.setProgress(0);
        }

        @Override
        protected Integer doInBackground(Object... params) {
            Integer res = 0;
            try {
                service.deleteGist(gistId);
                res = 1;
            } catch (IOException e) {
                e.printStackTrace();
                res = -1;
                init_snackbar(e.toString());
            }

            return res;
        }

        protected void onPostExecute(Integer result) {
            ////////////////Log.d(TAG, "Done!");

            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(100);

            description_et.setSelected(false);
            filename_et.setSelected(false);

            if (result == 1) {
                Toast.makeText(UpdateGistActivity.this, "Gist has been successfully deleted!", Toast.LENGTH_LONG).show();

                Intent data = new Intent();

                Bundle b = new Bundle();
                b.putString("deleted_gist_id", gistId);
                b.putBoolean("starred", gist_starred);
                b.putBoolean("public", gist_public);

                data.putExtras(b);
                setResult(2, data);
                finish();
            } else if (result != -1){
                init_snackbar("Gist deletion failed!");

                Toast.makeText(UpdateGistActivity.this, "Gist deletion failed!", Toast.LENGTH_LONG).show();
                mProgressBar.setVisibility(View.INVISIBLE);
                mProgressBar.setProgress(0);
            }
        }
    }
}
