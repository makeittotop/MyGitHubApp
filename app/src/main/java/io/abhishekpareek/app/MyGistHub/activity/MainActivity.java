package io.abhishekpareek.app.MyGistHub.activity;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.abhishekpareek.app.MyGistHub.R;
import io.abhishekpareek.app.MyGistHub.adapter.CustomAdapter;
import io.abhishekpareek.app.MyGistHub.adapter.CustomDrawerAdapter;
import io.abhishekpareek.app.MyGistHub.models.DrawerItem;
import io.abhishekpareek.app.MyGistHub.models.MyGist;
import io.abhishekpareek.app.MyGistHub.utils.Connectivity;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, AdapterView.OnItemClickListener {
    private String user;
    private GistService gistService;

    private TextView fetch_tv, total_gists_tv, no_gists_tv;
    //private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ListView myGists_lv;

    private List<MyGist> gistList= new ArrayList<>();
    private int total_gists = 0;
    private int total_pri_gists = 0;
    private int total_pub_gists = 0;
    private int total_starred_gists = 0;

    private List<MyGist> tmp_gistList= new ArrayList<>();
    private int tmp_total_gists = 0;
    private int tmp_total_pri_gists = 0;
    private int tmp_total_pub_gists = 0;
    private int tmp_total_starred_gists = 0;

    private PageIterator pageIterator = null;
    private int pageCount = 0;
    private int size = 25;

    private final String TAG = this.getClass().getSimpleName();

    private ArrayList<String> gistNames = new ArrayList<String>();
    private ArrayAdapter<String> gistsAdapter;
    private CustomAdapter mCustomAdapter;
    private String token;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CustomDrawerAdapter mCustomDrawerAdapter;
    private List<DrawerItem> dataList;

    private Snackbar mSnackbar;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init_ui();
    }

    private void init_snackbar() {
        mSnackbar = Snackbar
                .make(coordinatorLayout, "No internet connection!", Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY", this);
        mSnackbar.show();
    }

    private void init_ui() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                */
                launchCreateGistActivity();
            }
        });

        SharedPreferences prefs = getSharedPreferences(getResources().getString(R.string.token_str), MODE_PRIVATE);
        String token_str = getResources().getString(R.string.token_str);
        String user_str = getResources().getString(R.string.user_str);

        handleIntent(getIntent());

        fetch_tv = (TextView) findViewById(R.id.fetch_tv);
        total_gists_tv = (TextView) findViewById(R.id.total_gists);
        no_gists_tv = (TextView) findViewById(R.id.no_gists_tv);

        //mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refesh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        myGists_lv = (ListView) findViewById(R.id.myGists_lv);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        //mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, getResources().getStringArray(R.array.drawer_items_array)));

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setupDrawer();
        mCustomDrawerAdapter = new CustomDrawerAdapter(this, R.layout.drawer_list_item, dataList);
        mDrawerList.setAdapter(mCustomDrawerAdapter);
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());


        /*
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(getTitle());
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(getTitle());
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        if (savedInstanceState == null) {
            //selectItem(0);
        }
        */

        /*
        gistsAdapter = new ArrayAdapter<String>(this,
              android.R.layout.simple_list_item_1, android.R.id., gistNames);
        myGists_lv.setAdapter(gistsAdapter);
        */

        mCustomAdapter = new CustomAdapter(this, gistList, getResources());
        myGists_lv.setAdapter(mCustomAdapter);

        myGists_lv.setOnItemClickListener(null);

        /*
        myGists_lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int threshold = 1;
                int count = view.getCount();

                if (scrollState == SCROLL_STATE_IDLE) {
                    if (view.getLastVisiblePosition() >= count - threshold) {
                        //////////////////////////////////////Log.i(TAG, "loading more data");
                        // Execute LoadMoreDataTask AsyncTask
                        onRefresh();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        */

        //mProgressBar.setDrawingCacheBackgroundColor(getResources().getColor(R.color.blue));
        //mProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar));

        token = prefs.getString(token_str, null);
        user = prefs.getString(user_str, null);

        if (token == null) {
            //Login
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        } else {
            if (user != null)
                try {
                    init_github();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void launchUpdateGistActivity(int position) {
        // ListView Clicked item index
        int itemPosition     = position;
        // ListView Clicked item value
        //String  itemValue    = (String) myGists_lv.getItemAtPosition(position);
        // Show Alert
        //Toast.makeText(getApplicationContext(), "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG).show();
        // Launch the next activity
        Intent i = new Intent();
        Bundle b = new Bundle();
        b.putParcelable("myGist", (Parcelable) gistList.get(itemPosition));
        i.putExtras(b);
        i.setClass(MainActivity.this, UpdateGistActivity.class);

        startActivityForResult(i, 1);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
         String query =
               intent.getStringExtra(SearchManager.QUERY);
         doSearch(query);
      }
    }

    private void doSearch(String query) {
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        handleIntent(intent);
    }

    private void setupDrawerData() {
        dataList = new ArrayList<>();
        dataList.add(new DrawerItem("Refresh", R.drawable.ic_refresh_white_24dp));
        dataList.add(new DrawerItem("Add Gist", R.drawable.ic_note_add_white_24dp));
        dataList.add(new DrawerItem("Sign Out", R.drawable.ic_exit_to_app_white_24dp));
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(getTitle());
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(getTitle());
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        setupDrawerData();
    }

    private void init_github() throws IOException {
        //user = getResources().getString(R.string.username);
        gistService = new GistService();
        //gistService.getClient().setOAuth2Token(getResources().getString(R.string.access_token));
        gistService.getClient().setOAuth2Token(token);
        //new GithubRepoTask().execute(repositoryService);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                onRefresh();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(new ComponentName(this, MainActivity.class)));
        searchView.setIconifiedByDefault(false);

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mCustomAdapter.getFilter().filter(query);
                //////////////////////////////////////Log.d(TAG, "onQueryTextSubmit: " + query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mCustomAdapter.getFilter().filter(newText);
                //////////////////////////////////////Log.d(TAG, "onQueryTextChange: " + newText);

                return true;
            }
        };

        searchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_create)
            launchCreateGistActivity();
        else if (id == R.id.action_refresh) {
            onRefresh();
        }*/ if (id == R.id.action_logout) {
            signout();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        init_ui();
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final int pos = position;

        if (Connectivity.isConnected(getApplicationContext()) == false) {
            init_snackbar();
            return;
        } else {
            if (mSnackbar != null)
                mSnackbar.dismiss();
        }

        view.findViewById(R.id.popup_ll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                                    /*
                                    case R.id.star:
                                        Toast.makeText(MainActivity.this, "ToDo. Not implemented yet.", Toast.LENGTH_SHORT).show();
                                        break;
                                        */
                            case R.id.modify:
                                Toast.makeText(MainActivity.this, item.getTitle() + "!!", Toast.LENGTH_SHORT).show();
                                launchUpdateGistActivity(pos);
                                break;
                            case R.id.delete:
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        mSwipeRefreshLayout.setRefreshing(true);

                                        MyGist deleteGist = gistList.get(pos);
                                        String deleteGistId = deleteGist.getGistId();
                                        Boolean gist_public = deleteGist.isPublic();
                                        Boolean gist_starred = deleteGist.isStarred();

                                        new GithubGistSynchroniseTask().execute(gistService, deleteGistId, 2, gist_starred, gist_public, "delete");
                                    }})
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                            }
                                        });

                                AlertDialog dialog = builder.create();

                                dialog.setTitle("This gist will be permanently deleted. Are you sure?");
                                dialog.show();
                                break;
                        }

                        return false;
                    }
                });
                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.show();
            }
        });

        launchUpdateGistActivity(position);
    }


    private class SlideMenuClickListener implements ListView.OnItemClickListener {

        /**
         * Callback method to be invoked when an item in this AdapterView has
         * been clicked.
         * <p/>
         * Implementers can call getItemAtPosition(position) if they need
         * to access the data associated with the selected item.
         *
         * @param parent   The AdapterView where the click happened.
         * @param view     The view within the AdapterView that was clicked (this
         *                 will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         * @param id       The row id of the item that was clicked.
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayView(position);
        }
    }

    private void displayView(int position) {
        switch (position) {
            case 0:
                onRefresh();

                mDrawerList.setItemChecked(position, true);
                mDrawerList.setSelection(position);
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 1:
                launchCreateGistActivity();

                mDrawerList.setItemChecked(position, true);
                mDrawerList.setSelection(position);
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 2:
                signout();

                mDrawerList.setItemChecked(position, true);
                mDrawerList.setSelection(position);
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
        }
    }

    private void launchCreateGistActivity() {
        if (Connectivity.isConnected(getApplicationContext()) == false) {
            init_snackbar();
            return;
        } else {
            if (mSnackbar != null)
                mSnackbar.dismiss();
        }

        Intent i = new Intent();
        i.setClass(MainActivity.this, CreateGistActivity.class);
        startActivityForResult(i, 2);
    }

    private void signout() {
        SharedPreferences.Editor prefsEditor = getSharedPreferences(getResources().getString(R.string.token_str), MODE_PRIVATE).edit();
        prefsEditor.clear();
        prefsEditor.commit();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);

        //myGists_lv.setEnabled(false);
        if (fetch_tv.getVisibility() == View.GONE) {
            fetch_tv.setVisibility(View.VISIBLE);
            fetch_tv.setText("Fetching gists...");
        }

        if (total_gists_tv.getVisibility() == View.VISIBLE) {
            total_gists_tv.setVisibility(View.GONE);
        }

        new GithubGistFetchTask().execute(gistService);
    }

    private class GithubGistFetchTask extends AsyncTask<GistService, Integer, String> {

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
        protected String doInBackground(GistService... services) {
            tmp_total_gists = tmp_total_pub_gists = tmp_total_pri_gists = tmp_total_starred_gists = 0;
            tmp_gistList.clear();

            for (GistService service : services) {
                //////////////////////////////////////Log.d(TAG, String.format("User %s's Gists", user));
                int count = 0;
                try {
                    /*
                    if (pageIterator == null) {
                        pageIterator = service.pageGists(user, size);
                    }

                    //////////////////////////////////////Log.d(TAG, String.format("Page %d", ++pageCount));

                    if (pageIterator.hasNext()) {
                        Collection<Gist> collection = pageIterator.next();
                        for (Gist gist : collection) {
                            //////////////////////////////////////Log.d(TAG, String.format("%d: %s", count++, gist.getFiles()));
                            MyGist myGist = new MyGist();

                            Set set = gist.getFiles().entrySet();
                            // Get an iterator
                            Iterator i = set.iterator();
                            // Display elements
                            while (i.hasNext()) {
                                Map.Entry me = (Map.Entry) i.next();
                                String title = String.valueOf(me.getKey());
                                //////////////////////////////////////Log.d(TAG, title + ": ");
                                gistNames.add(title);

                                myGist.setGistTitle(title);
                                myGist.setGistId(gist.getId());
                                myGist.setGistDescription(gist.getDescription());
                                SimpleDateFormat simpleDate = new SimpleDateFormat("dd/M/y, HH:mm");
                                String strDt = simpleDate.format(gist.getUpdatedAt());
                                myGist.setGistUpdatedAt(strDt);
                                if (!gist.isPublic()) {
                                    myGist.setGistType("Private");
                                    total_pri_gists++;
                                } else {
                                    myGist.setGistType("Public");
                                    total_pub_gists++;
                                }
                                if (!service.isStarred(gist.getId())) {
                                    myGist.setStarred(false);
                                } else {
                                    myGist.setStarred(true);
                                    total_starred_gists++;
                                }
                            }

                            gistList.add(myGist);
                        }
                    }
                    */


                    List<Gist> gists = service.getGists(user);
                    tmp_total_gists = gists.size();

                    for (Gist gist : gists) {
                        //////////////////////////////////////Log.d(TAG, String.format("%d: %s", count++, gist.getFiles()));

                        MyGist myGist = new MyGist();

                        Set set = gist.getFiles().entrySet();
                        // Get an iterator
                        Iterator i = set.iterator();
                        // Display elements
                        while(i.hasNext()) {
                            Map.Entry me = (Map.Entry)i.next();
                            String title = String.valueOf(me.getKey());
                            //////////////////////////////////////Log.d(TAG, title + ": ");
                            gistNames.add(title);

                            myGist.setGistTitle(title);
                            myGist.setGistId(gist.getId());
                            myGist.setGistDescription(gist.getDescription());
                            SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/M/y, HH:mm");
                            String strDt = simpleDate.format(gist.getUpdatedAt());
                            myGist.setGistUpdatedAt(strDt);
                            if (!gist.isPublic()) {
                                myGist.setGistTypePublic(false);
                                tmp_total_pri_gists++;
                            }
                            else {
                                myGist.setGistTypePublic(true);
                                tmp_total_pub_gists++;
                            }
                            if (!service.isStarred(gist.getId())) {
                                myGist.setStarred(false);
                            } else {
                                myGist.setStarred(true);
                                tmp_total_starred_gists++;
                            }
                        }

                        publishProgress((int) ((count / (float) tmp_total_gists) * 100));

                        tmp_gistList.add(myGist);
                    }

                } catch (IOException e) {
                    e.printStackTrace();

                    runOnUiThread(
                            new Runnable() {
                                /**
                                 * Starts executing the active part of the class' code. This method is
                                 * called when a thread is started that has been created with a class which
                                 * implements {@code Runnable}.
                                 */
                                @Override
                                public void run() {
                                    init_snackbar();
                                }
                            });
                }

                //tmp_total_gists = tmp_gistList.size();
            }
            return "Done";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            fetch_tv.setText(String.format("Fetching gists... %d %%", values[0]));


            gistList.clear();
            gistList.addAll(tmp_gistList);

            mCustomAdapter.notifyDataSetChanged();

        }

        protected void onPostExecute(String result) {
            //////////////////////////////////////Log.d(TAG, "Done!");
            set_ui_post();
        }

    }

    private void set_ui_post() {
        total_gists = tmp_total_gists;
        total_pub_gists = tmp_total_pub_gists;
        total_pri_gists = tmp_total_pri_gists;

        if (total_gists > 0) {
            no_gists_tv.setVisibility(View.GONE);

            gistList.clear();
            gistList.addAll(tmp_gistList);
            //gistList = tmp_gistList;

            //mProgressBar.setVisibility(View.GONE);
            fetch_tv.setVisibility(View.GONE);
            myGists_lv.setOnItemClickListener(MainActivity.this);

            //myGists_lv.setEnabled(true);

            //total_gists_tv.setText(String.format("Total Gists: %d (Public: %d, Private: %d, starred: %d)", total_gists, total_pub_gists, total_pri_gists, total_starred_gists));
            //total_gists_tv.setText(String.format("Total Gists: %d (Public: %d, Private: %d)", total_gists, total_pub_gists, total_pri_gists));
            total_gists_tv.setText(String.format("Total Gists: %d (Public: %d)", total_gists, total_pub_gists));
            total_gists_tv.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setRefreshing(false);

            //Collections.reverse(gistNames);
            //Collections.reverse(gistList);
            mCustomAdapter.notifyDataSetChanged();
            //gistsAdapter.notifyDataSetChanged();
        } else {
            no_gists_tv.setVisibility(View.VISIBLE);
            fetch_tv.setVisibility(View.GONE);
            //total_gists_tv.setText(String.format("Total Gists: %d (Public: %d, Private: %d)", total_gists, total_pub_gists, total_pri_gists));
            total_gists_tv.setText(String.format("Total Gists: %d (Public: %d)", total_gists, total_pub_gists));
            total_gists_tv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == 1) {
                mSwipeRefreshLayout.setRefreshing(true);
                fetch_tv.setVisibility(View.VISIBLE);
                fetch_tv.setText("Synchronizing gists ..");

                Bundle b = data.getExtras();
                String updatedGistId = b.getString("updated_gist_id");

                new GithubGistSynchroniseTask().execute(gistService, updatedGistId, 1);
                //onRefresh();
            }
            else if (resultCode == 2){
                mSwipeRefreshLayout.setRefreshing(true);
                fetch_tv.setVisibility(View.VISIBLE);
                fetch_tv.setText("Synchronizing gists ..");

                Bundle b = data.getExtras();
                String deletedGistId = b.getString("deleted_gist_id");
                boolean gist_starred = b.getBoolean("starred");
                boolean gist_public = b.getBoolean("public");

                new GithubGistSynchroniseTask().execute(gistService, deletedGistId, 2, gist_starred, gist_public);
                //onRefresh();
            }
        } else if (requestCode == 2) {
            if (resultCode == 1) {
                mSwipeRefreshLayout.setRefreshing(true);
                fetch_tv.setVisibility(View.VISIBLE);
                fetch_tv.setText("Synchronizing gists ...");

                Bundle b = data.getExtras();
                String createdGistId = b.getString("created_gist_id");
                boolean gist_public = b.getBoolean("public");

                new GithubGistSynchroniseTask().execute(gistService, createdGistId, 3, null, gist_public);
                //onRefresh();
            }
        }
    }

    private class GithubGistSynchroniseTask extends AsyncTask<Object, Void, Boolean> {

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
        protected Boolean doInBackground(Object... params) {
            GistService service = (GistService) params[0];
            String id = (String) params[1];
            int type = (int) params[2];
            Boolean gist_starred = null;
            Boolean gist_public = null;
            String action = null;
            Boolean res = false;

            try {
                gist_starred = (Boolean) params[3];
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                gist_public = (Boolean) params[4];
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                action = (String) params[5];
            } catch (Exception e) {
                e.printStackTrace();
            }

            String filterString = id.toString().toLowerCase();
            final List<MyGist> list = tmp_gistList;
            int count = list.size();

            if (type == 1) {
                synchronizeUpdatedGist(service, id, filterString, list, count);
                res = true;
            } else if (type == 2) {
                // delete gist
                try {
                    if (action == "delete")
                        service.deleteGist(id);
                    synchronizeDeletedGist(gist_starred, gist_public, filterString, list, count);
                    res = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    init_snackbar();
                }
            } else {
                synchronizeCreatedGist(service, id, gist_public);
                res = true;
            }

            return res;
        }

        @Override
        protected void onPostExecute(Boolean res) {
            super.onPostExecute(res);

            if (res) {
                set_ui_post();
                /*
                total_gists_tv.setText(String.format("Total Gists: %d (Public: %d, Private: %d)", total_gists, total_pub_gists, total_pri_gists));
                if (total_gists > 0) {
                    no_gists_tv.setVisibility(View.GONE);

                    mSwipeRefreshLayout.setRefreshing(false);
                    fetch_tv.setVisibility(View.GONE);
                    //total_gists_tv.setText(String.format("Total Gists: %d (Public: %d, Private: %d, starred: %d)", total_gists, total_pub_gists, total_pri_gists, total_starred_gists));

                    gistList.clear();
                    gistList.addAll(tmp_gistList);
                    //mCustomAdapter.setData(tmp_gistList);
                    mCustomAdapter.notifyDataSetChanged();
                } else {

                    no_gists_tv.setVisibility(View.VISIBLE);
                }
                */
            }
        }
    }

    private void synchronizeCreatedGist(GistService service, String id, Boolean gist_public) {
        Gist createdGist = null;
        try {
            createdGist = service.getGist(id);
            MyGist myGist = new MyGist();

            Set set = createdGist.getFiles().entrySet();
            // Get an iterator
            Iterator it = set.iterator();
            // Display elements
            while (it.hasNext()) {
                Map.Entry me = (Map.Entry) it.next();
                String title = String.valueOf(me.getKey());

                myGist.setGistTitle(title);
                myGist.setGistId(createdGist.getId());
                myGist.setGistDescription(createdGist.getDescription());
                SimpleDateFormat simpleDate = new SimpleDateFormat("dd/M/y, HH:mm");
                String strDt = simpleDate.format(createdGist.getUpdatedAt());
                myGist.setGistUpdatedAt(strDt);
                if (!createdGist.isPublic()) {
                    myGist.setGistTypePublic(false);
                } else {
                    myGist.setGistTypePublic(true);
                }
                if (!service.isStarred(createdGist.getId())) {
                    myGist.setStarred(false);
                } else {
                    myGist.setStarred(true);
                }
            }

            tmp_gistList.add(0, myGist);
            tmp_total_gists++;

            if (gist_public != null)
                if (gist_public)
                    tmp_total_pub_gists++;
                else
                    tmp_total_pri_gists++;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void synchronizeDeletedGist(Boolean gist_starred, Boolean gist_public, String filterString, List<MyGist> list, int count) {
        String filterableString;

        for (int i = 0; i < count; i++) {
            MyGist myGist = list.get(i);
            // gist title
            filterableString = myGist.getGistId();
            if (filterableString.equals(filterString)) {
                //////////////////////////////////////Log.d("FILTER-MATCH", filterableString + " " + filterString);

                tmp_gistList.remove(i);

                tmp_total_gists--;

                if (gist_public != null)
                    if (gist_public)
                        tmp_total_pub_gists--;
                    else
                        tmp_total_pri_gists--;

                if (gist_starred != null)
                    if (gist_starred)
                        tmp_total_starred_gists--;

                break;
            }
        }
    }

    private void synchronizeUpdatedGist(GistService service, String id, String filterString, List<MyGist> list, int count) {
        try {
            Gist updatedGist = service.getGist(id);

            String filterableString;
            for (int i = 0; i < count; i++) {
                MyGist myGist = list.get(i);
                // gist title
                filterableString = myGist.getGistId();
                if (filterableString.equals(filterString)) {
                    //////////////////////////////////////Log.d("FILTER-MATCH", filterableString + " " + filterString);

                    myGist = new MyGist();

                    Set set = updatedGist.getFiles().entrySet();
                    // Get an iterator
                    Iterator it = set.iterator();
                    // Display elements
                    while (it.hasNext()) {
                        Map.Entry me = (Map.Entry) it.next();
                        String title = String.valueOf(me.getKey());

                        myGist.setGistTitle(title);
                        myGist.setGistId(updatedGist.getId());
                        myGist.setGistDescription(updatedGist.getDescription());
                        SimpleDateFormat simpleDate = new SimpleDateFormat("dd/M/y, HH:mm");
                        String strDt = simpleDate.format(updatedGist.getUpdatedAt());
                        myGist.setGistUpdatedAt(strDt);
                        if (updatedGist.isPublic()) {
                            myGist.setGistTypePublic(true);
                        } else {
                            myGist.setGistTypePublic(false);
                        }
                        if (!service.isStarred(updatedGist.getId())) {
                            myGist.setStarred(false);
                        } else {
                            myGist.setStarred(true);
                        }
                    }

                    tmp_gistList.set(i, myGist);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
