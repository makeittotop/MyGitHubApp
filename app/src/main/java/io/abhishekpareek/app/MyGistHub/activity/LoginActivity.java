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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import io.abhishekpareek.app.MyGistHub.R;
import io.abhishekpareek.app.MyGistHub.utils.Connectivity;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
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
    private static final String TAG = LoginActivity.class.getSimpleName();

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String token;
    private TextView github_join_tv;

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
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setupActionBar(toolbar);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.login_coordinator_layout);

        github_join_tv = (TextView) findViewById(R.id.github_join_tv);
        github_join_tv.setClickable(true);
        github_join_tv.setMovementMethod(LinkMovementMethod.getInstance());
        String url =  "<a href='https://www.github.com/join'>Join Github</a>";
        //text = getResources().getString(R.string.github_register_url);
        github_join_tv.setText(Html.fromHtml(url));
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

        if (id == R.id.action_login) {
            if (Connectivity.isConnected(this) == false) {
                init_snackbar("No Internet found!");
            } else {
                attemptLogin();
            }
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
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
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
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.length() > 0;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 6;
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
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (mSnackbar != null)
                if (mSnackbar.isShownOrQueued())
                    mSnackbar.dismiss();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Integer statusCode = null;
            /*
            try {
                OAuthService oauthService = new OAuthService();

                // Replace with actual login and password
                oauthService.getClient().setCredentials(mEmail, mPassword);

                // Create authorization with 'gist' scope only
                Authorization auth = new Authorization();
                auth.setScopes(Arrays.asList("gist"));
                auth = oauthService.createAuthorization(auth);

            } catch (IOException e) {
                e.printStackTrace();
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }
            */

            String strUrl = getResources().getString(R.string.API_BASE);
            //String strUrl = "https://gist.github.com/makeittotop/e384fee0ef55e29d7a5567a4a80778c4";

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(strUrl);

            httpPost.setHeader("User-Agent", "curl/7.29.0");

            String userPass = mEmail + ":" + mPassword;

            String basicAuth = "Basic " + Base64.encodeToString(userPass.getBytes(), Base64.NO_WRAP);
            httpPost.setHeader("Authorization", basicAuth);

            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");


            httpPost.setHeader("Content-Type", "application/json");

            StringEntity entity = null;

            String noteStr = String.format("%s %s", getResources().getString(R.string.note), String.valueOf(UUID.randomUUID()).split("-")[0]);
            try {
                entity = new StringEntity(String.format("{\"note\": \"%s\", \"scopes\": [\"gist\", \"repo\", \"user\"] }", noteStr)); //{\"key\":\"value\"}");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            httpPost.setEntity(entity);

            //making POST request.
            try {
                HttpResponse response = httpClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 201) {
                    ////////Log.d("Response: ", String.valueOf(response.getStatusLine().getStatusCode()));

                    String jsonResult = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = new JSONObject(jsonResult);

                    token = jsonObject.getString("token");
                    ////////Log.d("Response:", token);

                    statusCode = 201;
                } else if (response.getStatusLine().getStatusCode() == 422) {
                    statusCode = 422;
                } else {
                    // write response to log
                    String jsonResult = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = new JSONObject(jsonResult);

                    String message = jsonObject.getString("message");
                    /*
                    String errStr = String.valueOf(new JSONArray(jsonObject.getString("errors")).get(0));

                    HashMap<String,String> errMap;
                    if (errStr != null) {
    //                    errMap = new Gson().fromJson(errStr, new TypeToken<HashMap<String, String>>(){}.getType());
                    }
                    */
                    ////////Log.d("Response:", message);

                    if (message.contains("Bad credentials")) {
                        // unauthorized
                        statusCode = 401;
                    } else if (message.contains("two-factor")) {
                        // forbidden
                        statusCode = 403;
                    }
                }
            } catch (Exception e) {
                // Log exception
                e.printStackTrace();

                init_snackbar(e.toString());
            }

            /*
            try {
                URL url = new URL(strUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(false);

                //String userPass = mEmail + ":" + mPassword;
                String basicAuth = "Basic " + Base64.encodeToString(userPass.getBytes(), Base64.NO_WRAP);
                urlConnection.setRequestProperty("Authorization", basicAuth);
                urlConnection.setRequestProperty("User-Agent", "curl/7.29.0");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("X-GitHub-OTP", "408821");


                int status = urlConnection.getResponseCode();
                InputStream inputStream = (InputStream) urlConnection.getInputStream();
                BufferedReader in   =
                        new BufferedReader(new InputStreamReader (inputStream));
                String line;
                while ((line = in.readLine()) != null) {
                    ////////Log.d(TAG, line);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            */

            return statusCode;
        }

        @Override
        protected void onPostExecute(final Integer statusCode) {
            mAuthTask = null;
            showProgress(false);

            if (statusCode == null) {
                init_snackbar("No Internet found!");
            } else if (statusCode == 403) {
                Intent intent = new Intent();
                Bundle b = new Bundle();
                b.putString("email", mEmail);
                b.putString("password", mPassword);
                intent.putExtras(b);
                intent.setClass(LoginActivity.this, MFAActivity.class);
                startActivityForResult(intent, 1);

                finish();
            } else if (statusCode == 201) {
                SharedPreferences.Editor editor = getSharedPreferences(getResources().getString(R.string.token_str), MODE_PRIVATE).edit();
                editor.putString("user", mEmail);
                editor.putString("password", mPassword);
                editor.putString("token", token);
                editor.commit();

                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, MainActivity.class);
                startActivity(intent);

                finish();
            } else {
                //mPasswordView.setError(getString(R.string.error_incorrect_password));
                //mEmailView.setError(getString(R.string.error_invalid_email));

                Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_LONG).show();
                init_snackbar("Invalid credentials");
            }

            //mPasswordView.requestFocus();
            //mPasswordView.requestFocus();
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

