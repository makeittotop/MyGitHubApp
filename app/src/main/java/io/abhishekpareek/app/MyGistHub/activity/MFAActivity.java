package io.abhishekpareek.app.MyGistHub.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import io.abhishekpareek.app.MyGistHub.R;
import io.abhishekpareek.app.MyGistHub.utils.Connectivity;

/**
 * A login screen that offers login via email/password.
 */
public class MFAActivity extends AppCompatActivity  {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mfaView;
    private View mProgressView;
    private View mLoginFormView;

    private String email;
    private String password;
    private String mfa_code;

    private String token;
    private TextView github_mfa_tv;

    private Snackbar mSnackbar;
    private CoordinatorLayout coordinatorLayout;

    private void init_snackbar(String message) {
        mSnackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE);
        mSnackbar.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mfa);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setupActionBar(toolbar);

        // Set up the login form.
        mfaView = (AutoCompleteTextView) findViewById(R.id.mfa);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.mfa_coordinator_layout);

        Bundle b = getIntent().getExtras();
        email = b.getString("email");
        password = b.getString("password");

        github_mfa_tv = (TextView) findViewById(R.id.github_mfa_tv);
        github_mfa_tv.setClickable(true);
        github_mfa_tv.setMovementMethod(LinkMovementMethod.getInstance());
        String url =  "<a href='https://help.github.com/articles/about-two-factor-authentication/'>What\'s this?</a>";
        //text = getResources().getString(R.string.github_register_url);
        github_mfa_tv.setText(Html.fromHtml(url));
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     * @param toolbar
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar(Toolbar toolbar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_login)
            if (Connectivity.isConnected(this) == false) {
                init_snackbar("No Internet found!");
            } else {
                attemptLogin();
            }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mfaView.setError(null);

        // Store values at the time of the login attempt.
        mfa_code = mfaView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(mfa_code)) {
            mfaView.setError(getString(R.string.error_field_required));
            focusView = mfaView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(mfa_code);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isMfaValid(String mfa) {
        //TODO: Replace this with your own logic
        return mfa.contains("@");
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String mMfa;

        UserLoginTask(String mfa) {
            mMfa = mfa;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (mSnackbar != null)
                if (mSnackbar.isShownOrQueued())
                    mSnackbar.dismiss();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean res = false;

            String strUrl = getResources().getString(R.string.API_BASE);
            //strUrl = "http://www.reliply.org/tools/requestheaders.php";

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(strUrl);

            httpPost.setHeader("User-Agent", "curl/7.29.0");

            String userPass = email + ":" + password;

            String basicAuth = "Basic " + Base64.encodeToString(userPass.getBytes(), Base64.NO_WRAP);
            httpPost.setHeader("Authorization", basicAuth);
            httpPost.setHeader(new BasicHeader("X-GitHub-OTP", mMfa));
            httpPost.setHeader(new BasicHeader("X-foo", "565"));
            //httpPost.setHeader("Host", "api.github.com");
            httpPost.setHeader("Accept", "*/*");

            httpPost.setHeader("Content-Type", "application/json");

            StringEntity entity = null;

            String noteStr = String.format("%s %s", getResources().getString(R.string.note), String.valueOf(UUID.randomUUID()).split("-")[0]);
            try {
                entity = new StringEntity(String.format("{\"note\": \"%s\", \"scopes\": [\"gist\", \"repo\", \"user\"] }", noteStr)); //{\"key\":\"value\"}");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();

                init_snackbar(e.toString());
                return res;
            }

            httpPost.setEntity(entity);
            /*
            //Post Data
            List<NameValuePair> nameValuePair = new ArrayList<>(2);
            nameValuePair.add(new BasicNameValuePair("note", "Abhishek's Android App"));
            nameValuePair.add(new BasicNameValuePair("scopes", "gist"));

            //Encoding POST data
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // log exception
                e.printStackTrace();
            }
            */


            //making POST request.
            try {
                HttpResponse response = httpClient.execute(httpPost);
                ////Log.d("Response: ", String.valueOf(response.getStatusLine().getStatusCode()));



                String jsonResult = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = new JSONObject(jsonResult);

                token = jsonObject.getString("token");
                ////Log.d("Response:", token);

                if (token == null) {
                    res = false;
                } else  {
                    res = true;
                }
            } catch (Exception e) {
                // Log exception
                e.printStackTrace();

                init_snackbar(e.toString());
            }

            return res;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                SharedPreferences.Editor editor = getSharedPreferences(getResources().getString(R.string.token_str), MODE_PRIVATE).edit();
                editor.putString("user", email);
                editor.putString("password", password);
                editor.putString("token", token);
                editor.commit();

                Intent intent = new Intent();
                intent.setClass(MFAActivity.this, MainActivity.class);
                startActivity(intent);

                finish();
            } else {
                if (mSnackbar != null)
                    if (!mSnackbar.isShownOrQueued()) {
                        mfaView.setError(getString(R.string.error_incorrect_mfa));
                        mfaView.requestFocus();
                    } else
                        init_snackbar("No Internet found!");
                else
                    init_snackbar("No Internet found!");
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

