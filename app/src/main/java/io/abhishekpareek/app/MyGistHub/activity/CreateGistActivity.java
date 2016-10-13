package io.abhishekpareek.app.MyGistHub.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;

import java.io.IOException;
import java.util.Collections;

import io.abhishekpareek.app.MyGistHub.R;
import io.abhishekpareek.app.MyGistHub.utils.Connectivity;

public class CreateGistActivity extends AppCompatActivity {

    private EditText create_description_et;
    private EditText create_filename_et;
    private RadioButton public_rb;
    private RadioButton private_rb;
    private EditText create_contents_et;
    private ProgressBar mProgressBar;

    private GistService gistService;
    private Gist mGist;

    private static final String TAG = CreateGistActivity.class.getSimpleName();
    private Snackbar mSnackbar;
    private CoordinatorLayout coordinatorLayout;
    private boolean gist_starred = false, gist_public=false;
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
        setContentView(R.layout.activity_create_gist);

        init_ui();
    }

    private void init_ui() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.create_gist_coordinator);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        create_description_et = (EditText) findViewById(R.id.create_description_et);
        create_filename_et = (EditText) findViewById(R.id.create_filename_et);
        public_rb = (RadioButton) findViewById(R.id.public_rb);
        private_rb = (RadioButton) findViewById(R.id.private_rb);
        create_contents_et = (EditText) findViewById(R.id.create_contents_et);
        star_cb = (CheckBox) findViewById(R.id.create_star_cb);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar3);
        mProgressBar.setProgress(0);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.INVISIBLE);

        SharedPreferences prefs = getSharedPreferences(getResources().getString(R.string.token_str), MODE_PRIVATE);
        String token_str = getResources().getString(R.string.token_str);
        String token = prefs.getString(token_str, "");

        gistService = new GistService();
        gistService.getClient().setOAuth2Token(token);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create, menu);
        return true;
    }

        @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_create)
            if (Connectivity.isConnected(this) == false) {
                init_snackbar("No Internet found!");
            } else {
                createGist();
            }

        return super.onOptionsItemSelected(item);
    }

    private void createGist() {
        if (String.valueOf(create_filename_et.getText()).equals("") || String.valueOf(create_contents_et.getText()).equals("")) {
            init_snackbar("Fields can't be blank");
            return;
        }

        GistFile file = new GistFile();
        file.setContent(String.valueOf(create_contents_et.getText()));

        mGist = new Gist();
        mGist.setDescription(String.valueOf(create_description_et.getText()));

        mGist.setFiles(Collections.singletonMap(String.valueOf(create_filename_et.getText()), file));

        if (public_rb.isChecked())
            mGist.setPublic(true);
        else if (private_rb.isChecked())
            mGist.setPublic(false);

        if (star_cb.isChecked())
            mStarGist = true;

        mProgressBar.setVisibility(View.VISIBLE);

        new GithubGistUpdateTask().execute();

    }

    private class GithubGistUpdateTask extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            Gist g = mGist;
            Integer res = 0;
            try {
                mGist = gistService.createGist(g);
                gistService.starGist(mGist.getId());
                res = 1;
            } catch (IOException e) {
                e.printStackTrace();
                res = -1;
                init_snackbar(e.toString());
            }

            return res;
        }

        protected void onPostExecute(Integer result) {
            //Log.d(TAG, "Done!");

            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(100);

            if(result == 1) {
                Toast.makeText(CreateGistActivity.this, "Gist has been successfully created!", Toast.LENGTH_LONG).show();

                String createdGistId = mGist.getId();
                gist_public = mGist.isPublic();
                gist_starred = mStarGist;

                Intent data = new Intent();
                Bundle b = new Bundle();
                b.putString("created_gist_id", createdGistId);
                b.putBoolean("starred", gist_starred);
                b.putBoolean("public", gist_public);

                data.putExtras(b);
                setResult(1, data);
                finish();
            }
            else if (result != -1){
                init_snackbar("Gist creation failed!");

                Toast.makeText(CreateGistActivity.this, "Gist creation failed!", Toast.LENGTH_LONG).show();
                mProgressBar.setProgress(0);
                mProgressBar.setIndeterminate(true);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }
}
