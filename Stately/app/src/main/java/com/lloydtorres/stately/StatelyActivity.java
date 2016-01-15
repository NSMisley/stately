package com.lloydtorres.stately;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.RoundedImageView;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.nation.NationFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class StatelyActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final String APP_TAG = "com.lloydtorres.stately";
    private final String BANNER_TEMPLATE = "http://www.nationstates.net/images/banners/%s.jpg";

    private DrawerLayout drawer;
    private NavigationView navigationView;

    private Nation mNation;
    private ImageView nationBanner;
    private RoundedImageView nationFlag;
    private TextView nationNameView;

    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stately);

        if (getIntent() != null)
        {
            mNation = (Nation) getIntent().getParcelableExtra("mNationData");
        }
        if (mNation == null && savedInstanceState != null)
        {
            mNation = savedInstanceState.getParcelable("mNationData");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_generic);
        setToolbar(toolbar);
        initNavigationView();
    }

    public void setToolbar(Toolbar t)
    {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("");

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, t, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    private void initNavigationView()
    {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        initNavBanner();
        startNationFragment();
    }

    private void initNavBanner()
    {
        View view = navigationView.getHeaderView(0);
        nationBanner = (ImageView) view.findViewById(R.id.nav_banner_back);
        nationFlag = (RoundedImageView) view.findViewById(R.id.nav_flag);
        nationNameView = (TextView) view.findViewById(R.id.nav_nation_name);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        ImageLoader imageLoader = ImageLoader.getInstance();

        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder().displayer(new FadeInBitmapDisplayer(500)).build();

        nationNameView.setText(mNation.name);
        imageLoader.displayImage(String.format(BANNER_TEMPLATE, mNation.bannerKey), nationBanner, imageOptions);
        imageLoader.displayImage(mNation.flagURL, nationFlag, imageOptions);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        if (mNation != null)
        {
            savedInstanceState.putParcelable("mNationData", mNation);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable("mNationData");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        if (id != currentPosition)
        {
            currentPosition = id;
            if (id == R.id.nav_nation) {
                startNationFragment();
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        else
        {
            return false;
        }
    }
    private void startNationFragment()
    {
        getSupportActionBar().hide();
        NationFragment nationFragment = new NationFragment();
        nationFragment.setNation(mNation);

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.coordinator_generic, nationFragment)
                .commit();
    }
}
