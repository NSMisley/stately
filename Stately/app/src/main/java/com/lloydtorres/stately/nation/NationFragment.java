package com.lloydtorres.stately.nation;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.lloydtorres.stately.helpers.PrimeActivity;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Nation;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * Created by Lloyd on 2016-01-13.
 */
public class NationFragment extends Fragment {
    private final String APP_TAG = "com.lloydtorres.stately";
    private final String BANNER_TEMPLATE = "http://www.nationstates.net/images/banners/%s.jpg";

    private final int OVERVIEW_TAB = 0;
    private final int PEOPLE_TAB = 1;
    private final int GOV_TAB = 2;
    private final int ECONOMY_TAB = 3;
    private final int HAPPEN_TAB = 4;

    private Nation mNation;

    private OverviewSubFragment overviewSubFragment;
    private PeopleSubFragment peopleSubFragment;
    private GovernmentSubFragment governmentSubFragment;
    private EconomySubFragment economySubFragment;

    // variables used for nation views
    private TextView nationName;
    private TextView nationPrename;
    private ImageView nationBanner;
    private RoundedImageView nationFlag;
    private TextView waState;

    // variables used for tabs
    private PagerSlidingTabStrip tabs;
    private ViewPager tabsPager;
    private LayoutAdapter tabsAdapter;

    private Toolbar toolbar;
    private Activity mActivity;

    public void setNation(Nation n)
    {
        mNation = n;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_nation, container, false);

        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable("mNationData");
        }

        if (mNation != null)
        {
            initToolbar(view);
            getAllNationViews(view);
            initNationData(view);
        }

        return view;
    }

    private void initToolbar(View view)
    {
        toolbar = (Toolbar) view.findViewById(R.id.toolbar_nation);

        if (mActivity instanceof PrimeActivity)
        {
            ((PrimeActivity) mActivity).setToolbar(toolbar);
        }

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_container);
        collapsingToolbarLayout.setTitle("");

        AppBarLayout appBarLayout = (AppBarLayout) view.findViewById(R.id.nation_appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset <= 0) {
                    if (mNation != null) {
                        collapsingToolbarLayout.setTitle(mNation.name);
                    }
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle("");
                    isShow = false;
                }
            }
        });
    }

    public Toolbar getToolbar()
    {
        return toolbar;
    }

    private void initTabs(View view)
    {
        // Initialize the ViewPager and set an adapter
        tabsPager = (ViewPager) view.findViewById(R.id.nation_pager);
        tabsAdapter = new LayoutAdapter(getChildFragmentManager());
        tabsPager.setAdapter(tabsAdapter);
        // Bind the tabs to the ViewPager
        tabs = (PagerSlidingTabStrip) view.findViewById(R.id.nation_tabs);
        tabs.setViewPager(tabsPager);
    }

    private void getAllNationViews(View view)
    {
        nationName = (TextView) view.findViewById(R.id.nation_name);
        nationPrename = (TextView) view.findViewById(R.id.nation_prename);
        nationBanner = (ImageView) view.findViewById(R.id.nation_banner);
        nationFlag = (RoundedImageView) view.findViewById(R.id.nation_flag);
        waState = (TextView) view.findViewById(R.id.nation_wa_status);
    }

    public void initNationData(View view) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext()).build();
        ImageLoader.getInstance().init(config);
        ImageLoader imageLoader = ImageLoader.getInstance();

        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder().displayer(new FadeInBitmapDisplayer(500)).build();

        nationName.setText(mNation.name);
        nationPrename.setText(Html.fromHtml(mNation.prename).toString());
        imageLoader.displayImage(String.format(BANNER_TEMPLATE, mNation.bannerKey), nationBanner, imageOptions);
        imageLoader.displayImage(mNation.flagURL, nationFlag, imageOptions);

        if (mNation.waState.equals(getString(R.string.nation_wa_member)))
        {
            waState.setVisibility(View.VISIBLE);
        }

        overviewSubFragment = new OverviewSubFragment();
        overviewSubFragment.setNation(mNation);

        peopleSubFragment = new PeopleSubFragment();
        peopleSubFragment.setNation(mNation);

        governmentSubFragment = new GovernmentSubFragment();
        governmentSubFragment.setNation(mNation);

        economySubFragment = new EconomySubFragment();
        economySubFragment.setNation(mNation);

        initTabs(view);
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
    public void onDestroy()
    {
        super.onDestroy();
        mActivity = null;
    }

    // For formatting the tab slider
    public class LayoutAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = getResources().getStringArray(R.array.nation_tabs);

        public LayoutAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position)
            {
                case OVERVIEW_TAB:
                    return overviewSubFragment;
                case PEOPLE_TAB:
                    return peopleSubFragment;
                case GOV_TAB:
                    return governmentSubFragment;
                case ECONOMY_TAB:
                    return economySubFragment;
                default:
                    return new Fragment();
            }
        }
    }
}
