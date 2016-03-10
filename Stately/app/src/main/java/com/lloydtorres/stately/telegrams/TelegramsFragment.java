/**
 * Copyright 2016 Lloyd Torres
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lloydtorres.stately.telegrams;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Telegram;
import com.lloydtorres.stately.dto.TelegramFolder;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.MuffinsHelper;
import com.lloydtorres.stately.helpers.PrimeActivity;
import com.lloydtorres.stately.helpers.SparkleHelper;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Lloyd on 2016-03-08.
 * This is the Telegrams section of the main Stately activity.
 */
public class TelegramsFragment extends Fragment {
    public static final String KEY_PAST_OFFSET = "keyPastOffset";
    public static final String KEY_TELEGRAMS = "keyTelegrams";
    public static final String KEY_FOLDERS = "keyFolders";
    public static final String KEY_ACTIVE = "keyActive";

    // Direction to scan for messages
    private static final int SCAN_BACKWARD = 0;
    private static final int SCAN_FORWARD = 1;
    private static final int SCAN_SAME = 2;

    private Activity mActivity;
    private View mView;
    private Toolbar toolbar;
    private SwipyRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private ArrayList<Telegram> telegrams;
    private ArrayList<TelegramFolder> folders;
    private TelegramFolder activeFolder;
    private Set<Integer> uniqueEnforcer;
    private int pastOffset = 0;

    @Override
    public void onAttach(Context context) {
        // Get activity for manipulation
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.content_message_board, container, false);
        telegrams = new ArrayList<Telegram>();
        folders = new ArrayList<TelegramFolder>();
        activeFolder = new TelegramFolder();
        activeFolder.name = "Inbox";
        activeFolder.value = "inbox";
        uniqueEnforcer = new HashSet<Integer>();

        // Restore state
        if (savedInstanceState != null)
        {
            pastOffset = savedInstanceState.getInt(KEY_PAST_OFFSET, 0);
            telegrams = savedInstanceState.getParcelableArrayList(KEY_TELEGRAMS);
            folders = savedInstanceState.getParcelableArrayList(KEY_FOLDERS);
            activeFolder = savedInstanceState.getParcelable(KEY_ACTIVE);
            rebuildUniqueEnforcer();
        }

        toolbar = (Toolbar) mView.findViewById(R.id.message_board_toolbar);
        toolbar.setTitle(getString(R.string.menu_telegrams));

        if (mActivity != null && mActivity instanceof PrimeActivity)
        {
            ((PrimeActivity) mActivity).setToolbar(toolbar);
        }

        // Set up refresher to reload data on refresh
        mSwipeRefreshLayout = (SwipyRefreshLayout) mView.findViewById(R.id.message_board_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (direction.equals(SwipyRefreshLayoutDirection.TOP))
                {
                    queryTelegrams(0, SCAN_FORWARD);
                }
                else
                {
                    queryTelegrams(pastOffset, SCAN_BACKWARD);
                }
            }
        });

        // Setup recyclerview
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.message_board_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        return mView;
    }

    /**
     * Call to start querying and activate SwipeFreshLayout
     * @param direction Direction to scan in
     */
    public void startQueryTelegrams(final int direction)
    {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                queryTelegrams(0, direction);
            }
        });
    }

    /**
     * Scrape and parse telegrams from NS site.
     */
    private void queryTelegrams(final int offset, final int direction)
    {
        String targetURL = String.format(Telegram.GET_TELEGRAM, activeFolder.value, offset);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (getActivity() == null || !isAdded())
                        {
                            return;
                        }
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        processRawTelegrams(d, direction);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (getActivity() == null || !isAdded())
                {
                    return;
                }
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_generic));
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                if (getActivity() != null && isAdded())
                {
                    UserLogin u = SparkleHelper.getActiveUser(getContext());
                    params.put("User-Agent", String.format(getString(R.string.app_header), u.nationId));
                    params.put("Cookie", String.format("autologin=%s", u.autologin));
                }
                return params;
            }
        };

        if (!DashHelper.getInstance(getContext()).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(mView, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Actually parse through the response sent by NS and build telegram objects.
     * @param d Document containing parsed response.
     * @param direction Direction the user is loading telegrams
     */
    private void processRawTelegrams(Document d, int direction)
    {
        Element telegramsContainer = d.select("div#tglist").first();
        Element foldersContainer = d.select("select#tgfolder").first();

        if (telegramsContainer == null || foldersContainer == null)
        {
            // safety check
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(mView, getString(R.string.login_error_parsing));
            return;
        }

        // Build list of folders
        folders = new ArrayList<TelegramFolder>();
        Elements rawFolders = foldersContainer.select("option[value]");
        for (Element rf : rawFolders)
        {
            TelegramFolder telFolder = new TelegramFolder();
            String rfValue = rf.attr("value");
            if (!rfValue.equals("_new"))
            {
                String rfName = rf.text();
                telFolder.name = rfName;
                telFolder.value = rfValue;
                folders.add(telFolder);
            }
        }

        // Build telegram objects from raw telegrams
        ArrayList<Telegram> scannedTelegrams = MuffinsHelper.processRawTelegrams(telegramsContainer, SparkleHelper.getActiveUser(getContext()).nationId, true);
        // @TODO: Do something with the scanned telegrams
        processTelegramsForward(scannedTelegrams);

        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Processes the scanned telegrams if scanning forward (i.e. new telegrams).
     * @param scannedTelegrams Telegrams scanned from NS
     */
    private void processTelegramsForward(ArrayList<Telegram> scannedTelegrams)
    {
        int uniqueMessages = 0;

        for (Telegram t : scannedTelegrams)
        {
            if (!uniqueEnforcer.contains(t.id))
            {
                telegrams.add(t);
                uniqueEnforcer.add(t.id);
                uniqueMessages++;
            }
        }

        if (uniqueMessages <= 0)
        {
            SparkleHelper.makeSnackbar(mView, getString(R.string.rmb_caught_up));
        }

        // We've reached the point where we already have the messages, so put everything back together
        // @TODO: Modularize
        //refreshRecycler(SCAN_FORWARD);
        mRecyclerAdapter = new TelegramsAdapter(getContext(), telegrams);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * This function rebuilds the set used to track unique messages after a restart.
     * Because set isn't parcelable :(
     */
    private void rebuildUniqueEnforcer()
    {
        uniqueEnforcer = new HashSet<Integer>();
        for (Telegram t : telegrams)
        {
            uniqueEnforcer.add(t.id);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(KEY_PAST_OFFSET, pastOffset);
        if (telegrams != null)
        {
            savedInstanceState.putParcelableArrayList(KEY_TELEGRAMS, telegrams);
        }
        if (folders != null)
        {
            savedInstanceState.putParcelableArrayList(KEY_FOLDERS, folders);
        }
        if (activeFolder != null)
        {
            savedInstanceState.putParcelable(KEY_ACTIVE, activeFolder);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_telegrams, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getChildFragmentManager();
        switch (item.getItemId()) {
            case R.id.nav_folders:
                return true;
            case R.id.nav_compose:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy()
    {
        // Detach activity on destroy
        super.onDestroy();
        mActivity = null;
    }
}
