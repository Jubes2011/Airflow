package com.airstem.airflow.ayush.airflow;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.airstem.airflow.ayush.airflow.adapters.collection.TrackAdapter;
import com.airstem.airflow.ayush.airflow.adapters.home.DiscoverAdapter;
import com.airstem.airflow.ayush.airflow.events.collection.CollectionTrackListener;
import com.airstem.airflow.ayush.airflow.events.home.DiscoverListener;
import com.airstem.airflow.ayush.airflow.helpers.collection.CollectionConstant;
import com.airstem.airflow.ayush.airflow.helpers.internet.InternetHelper;
import com.airstem.airflow.ayush.airflow.model.collection.CollectionTrack;
import com.airstem.airflow.ayush.airflow.model.home.Discover;
import com.airstem.airflow.ayush.airflow.model.home.DiscoverItem;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CollectionTrackListener, DiscoverListener {

    boolean isLoading = false;
    InternetHelper internetHelper;
    ProgressDialog progressDialog;


    Toolbar toolbar;
    NavigationView navigationView;
    FloatingActionButton fab;
    FloatingSearchView mSearchView;
    CoordinatorLayout coordinatorLayout;
    protected DrawerLayout drawerLayout;


    TextView empty;
    RecyclerView listView;
    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayoutManager linearLayoutManager;



    //when internet
    ArrayList<Discover> mItems;
    DiscoverAdapter mDiscoverAdapter;
    //when no internet
    ArrayList<CollectionTrack> mTracks;
    TrackAdapter mTrackAdapter;


    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init components
        initComponent();
    }

    private void initComponent() {


        progressDialog = new ProgressDialog(MainActivity.this);
        internetHelper = new InternetHelper(MainActivity.this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        fab = (FloatingActionButton) findViewById(R.id.activity_main_fab);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_main_coordinate_layout);
        empty = (TextView) findViewById(R.id.activity_main_empty);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_refresh);
        listView = (RecyclerView) findViewById(R.id.activity_main_list);
        mSearchView = (FloatingSearchView) findViewById(R.id.activity_main_search_view);

        listView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        listView.setLayoutManager(linearLayoutManager);



        /*mAdView = (AdView) findViewById(R.id.activity_main_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                viewPager.setPadding(0,0,0,0);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                int heightDp = getHeight();
                if(heightDp <= 400)
                    viewPager.setPadding(0,0,0,32);
                else if(heightDp > 400 &&heightDp <= 720)
                    viewPager.setPadding(0,0,0,50);
                else if(heightDp > 720)
                    viewPager.setPadding(0,0,0,90);
                else
                    viewPager.setPadding(0,0,0,0);
            }
        });*/

        //set click listeners
        setListeners();

        //load data
    }

    private void setListeners() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_discover);
        mSearchView.attachNavigationDrawerToMenuButton(drawerLayout);

        setAdapter();
        makeRequest(false);



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                int y = 1;
                //get suggestions based on newQuery

                //pass them on to the search view
                mSearchView.swapSuggestions(new ArrayList<SearchSuggestion>());
            }
        });

        mSearchView.setOnLeftMenuClickListener(new FloatingSearchView.OnLeftMenuClickListener() {
            @Override
            public void onMenuOpened() {
                drawerLayout.openDrawer(GravityCompat.START);
            }

            @Override
            public void onMenuClosed() {
                int y = 1;
            }
        });
        mSearchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {
                int y = 1;
            }
        });
        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_speech) {
                    getTextFromSpeech();
                } else if (id == R.id.action_settings) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                } else if (id == R.id.action_share) {

                }else if (id == R.id.action_rate) {

                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                makeRequest(true);
            }
        });

    }

    private void setAdapter() {
        mItems = new ArrayList<>();
        mTracks = new ArrayList<>();
        mTrackAdapter = new TrackAdapter(MainActivity.this, mTracks, this);
        mDiscoverAdapter = new DiscoverAdapter(MainActivity.this, mItems, this);
    }

    public void makeRequest(boolean showDialog) {
        if (internetHelper.isNetworkAvailable()) {
            listView.setAdapter(mDiscoverAdapter);
            onNetworkAvailable(showDialog);
        } else {
            //changes here show last played tracks when network not available
            listView.setAdapter(mTrackAdapter);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void onNetworkAvailable(final boolean showDialog) {
        loadData(showDialog);
        empty.setVisibility(View.GONE);
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= lastVisibleItem) {
                    loadData(showDialog);
                }
            }
        });
    }


    private void loadData(boolean showDialog) {
        try {
            isLoading = true;
            if (showDialog) {
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

        } catch (Exception e) {
            isLoading = false;
            empty.setVisibility(View.VISIBLE);

            e.printStackTrace();
        }
    }


    private void askPermission() {
        int storagePermissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission_group.STORAGE);
        int internetPermissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET);
        int lockPermissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WAKE_LOCK);
        if (storagePermissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission_group.STORAGE}, CollectionConstant.PERMISSION_REQUEST);
        } else if (internetPermissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, CollectionConstant.PERMISSION_REQUEST);
        } else if (lockPermissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WAKE_LOCK}, CollectionConstant.PERMISSION_REQUEST);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CollectionConstant.PERMISSION_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];
                if(permission.equals(Manifest.permission_group.STORAGE) && grantResult != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission_group.STORAGE}, CollectionConstant.PERMISSION_REQUEST);
                } else if (permission.equals(Manifest.permission.INTERNET) && grantResult != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, CollectionConstant.PERMISSION_REQUEST);
                } else if (permission.equals(Manifest.permission.WAKE_LOCK) && grantResult != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WAKE_LOCK}, CollectionConstant.PERMISSION_REQUEST);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CollectionConstant.NETWORK_CODE) {
            if (internetHelper.isNetworkAvailable()) {
                showSnackBar("You are connected.");
            } else {
                showNetworkAlert(MainActivity.this);
            }
        } else if (requestCode == CollectionConstant.SPEECH_CODE) {
            ArrayList<String> stringArrayList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String textSpoke = String.valueOf(stringArrayList.get(0));
        } else {
            showSnackBar("Something went wrong. Try again.");
        }
    }

    private void showNetworkAlert(Context context) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Couldn't Connect, Try again");
        alert.setMessage("You need a network connection to use airflow. Please connect your mobile network or WiFi.");
        alert.setPositiveButton("SETTINGS",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_SETTINGS), CollectionConstant.NETWORK_CODE);
                    }
                });
        alert.setNegativeButton("CANCEL", null);
        alert.show();
    }


    private int getHeight() {
        int heightDp = 0;
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        heightDp = size.y;
        return heightDp;
    }

    private void showSnackBar(String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_discover) {
            startActivity(new Intent(MainActivity.this, MainActivity.class));
        } else if (id == R.id.nav_favorites) {
            startActivity(new Intent(MainActivity.this, FavActivity.class));
        }else if (id == R.id.nav_music_library) {
            startActivity(new Intent(MainActivity.this, CollectionActivity.class));
        } else if (id == R.id.nav_releases) {
            startActivity(new Intent(MainActivity.this, FavActivity.class));
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    public void getTextFromSpeech() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, CollectionConstant.SPEECH_TEXT);
        try {
            startActivityForResult(intent, CollectionConstant.SPEECH_CODE);
        } catch (ActivityNotFoundException e) {
            showSnackBar("Speech to text not supported.");
        }
    }

    @Override
    public void onTrackClick(CollectionTrack collectionTrack) {

    }

    @Override
    public void onTrackRemove(CollectionTrack collectionTrack) {

    }

    @Override
    public void onTrackFav(CollectionTrack collectionTrack, boolean addToFav) {

    }

    @Override
    public void onMoreClick() {

    }

    @Override
    public void onClick(DiscoverItem discoverItem) {

    }
}
