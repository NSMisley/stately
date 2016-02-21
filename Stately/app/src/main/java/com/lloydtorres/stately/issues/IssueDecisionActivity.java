package com.lloydtorres.stately.issues;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Issue;
import com.lloydtorres.stately.dto.IssueOption;
import com.lloydtorres.stately.dto.UserLogin;
import com.lloydtorres.stately.helpers.DashHelper;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lloyd on 2016-01-28.
 * This activity displays options for a particular issue.
 */
public class IssueDecisionActivity extends AppCompatActivity {
    // Keys for Intent data
    public static final String ISSUE_DATA = "issueData";
    private static final String DISMISS_TEXT = "The government is preparing to dismiss this issue.";
    public static final int NO_JUMP = -2;
    public static final int DISMISSED = -1;

    private Issue issue;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_decision);

        SparkleHelper.initAd(findViewById(R.id.activity_issue_decision_main), R.id.ad_decision_activity);

        // Either get data from intent or restore state
        if (getIntent() != null)
        {
            issue = getIntent().getParcelableExtra(ISSUE_DATA);
        }
        if (savedInstanceState != null)
        {
            issue = savedInstanceState.getParcelable(ISSUE_DATA);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.issue_decision_toolbar);
        setToolbar(toolbar);

        // Setup refresher to requery for resolution on swipe
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.issue_decision_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryIssueInfo(NO_JUMP);
            }
        });

        // Setup recyclerview
        mRecyclerView = (RecyclerView) findViewById(R.id.issue_decision_recycler);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    public void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);

        // Need to be able to get back to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * Call to start querying and activate SwipeFreshLayout
     * @param jumpPos position to jump to on load
     */
    private void startQueryIssueInfo(final int jumpPos)
    {
        // hack to get swiperefreshlayout to show initially while loading
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                queryIssueInfo(jumpPos);
            }
        });
    }

    /**
     * Query information on the current issue from the actual NationStates site
     * @param jumpPos position to jump to on load
     */
    private void queryIssueInfo(final int jumpPos)
    {
        final View view = findViewById(R.id.issue_decision_main);
        String targetURL = String.format(IssueOption.QUERY, issue.id);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document d = Jsoup.parse(response, SparkleHelper.BASE_URI);
                        processIssueInfo(view, d, jumpPos);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                UserLogin u = SparkleHelper.getActiveUser(getBaseContext());
                params.put("Cookie", String.format("autologin=%s", u.autologin));
                return params;
            }
        };

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    /**
     * Process the received page into the Issue and its IssueOptions
     * @param v Activity view
     * @param d Document received from NationStates
     * @param jumpPos position to jump to on load
     */
    private void processIssueInfo(View v, Document d, int jumpPos)
    {
        Element issueInfoContainer = d.select("div#dilemma").first();

        if (issueInfoContainer == null)
        {
            // safety check
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(v, getString(R.string.login_error_parsing));
            return;
        }

        Elements issueInfoRaw = issueInfoContainer.children();

        String issueText = issueInfoRaw.select("p").first().text();
        issue.content = issueText;

        issue.options = new ArrayList<IssueOption>();
        Elements optionsHolder = issueInfoRaw.select("ol.diloptions").first().getElementsByTag("li");

        int i = 0;
        for (Element option : optionsHolder)
        {
            IssueOption issueOption = new IssueOption();
            issueOption.index = i++;

            Element button = option.getElementsByTag("button").first();
            if (button != null)
            {
                issueOption.header = button.attr("name");
            }
            else
            {
                issueOption.header = IssueOption.SELECTED_HEADER;
            }

            String optionContent = option.getElementsByTag("p").first().text();
            issueOption.content = optionContent;

            if (option.hasClass("chosendiloption"))
            {
                issueOption.selected = true;
            }

            issue.options.add(issueOption);
        }

        IssueOption dismissOption = new IssueOption();
        dismissOption.index = -1;
        dismissOption.header = IssueOption.DISMISS_HEADER;
        dismissOption.content = "";
        if (issueInfoRaw.text().contains(DISMISS_TEXT))
        {
            dismissOption.selected = true;
        }
        issue.options.add(dismissOption);

        mRecyclerAdapter = new IssueDecisionRecyclerAdapter(this, issue);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        switch (jumpPos)
        {
            case DISMISSED:
                mLayoutManager.scrollToPosition(issue.options.size() - 1);
                break;
            case NO_JUMP:
                break;
            default:
                mLayoutManager.scrollToPosition(jumpPos + 1);
        }

        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setEnabled(false);
    }

    /**
     * Helper to confirm the position selected by the user.
     * @param index The index of the option selected.
     * @param header The header request of the option selected.
     */
    public void setAdoptPosition(final int index, final String header)
    {
        SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(this);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                postAdoptPosition(index, header);
                dialog.dismiss();
            }
        };

        if (storage.getBoolean("setting_issueconfirm", true))
        {
            dialogBuilder
                    .setNegativeButton(getString(R.string.explore_negative), null);

            switch (index)
            {
                case DISMISSED:
                    dialogBuilder.setTitle(getString(R.string.issue_option_confirm_dismiss))
                            .setPositiveButton(getString(R.string.issue_option_dismiss), dialogClickListener);
                    break;
                default:
                    dialogBuilder.setTitle(String.format(getString(R.string.issue_option_confirm_adopt), index + 1))
                            .setPositiveButton(getString(R.string.issue_option_adopt), dialogClickListener);
                    break;
            }

            dialogBuilder.show();
        }
        else
        {
            postAdoptPosition(index, header);
        }
    }

    /**
     * Send the position selected by the user back to the server.
     * @param index The index of the option selected.
     * @param header The header request of the option selected.
     */
    public void postAdoptPosition(final int index, final String header)
    {
        final View view = findViewById(R.id.issue_decision_main);
        String targetURL = String.format(IssueOption.QUERY, issue.id);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, targetURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        /*
                        switch (index)
                        {
                            case DISMISSED:
                                SparkleHelper.makeSnackbar(view, getString(R.string.issue_dismissed_message));
                                break;
                            default:
                                SparkleHelper.makeSnackbar(view, String.format(getString(R.string.issue_selected_message), index+1));
                                break;
                        }
                        startQueryIssueInfo(index);*/
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(view, getString(R.string.login_error_generic));
                }
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put(header, "1");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<String, String>();
                UserLogin u = SparkleHelper.getActiveUser(getBaseContext());
                params.put("Cookie", String.format("autologin=%s", u.autologin));
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        if (!DashHelper.getInstance(this).addRequest(stringRequest))
        {
            mSwipeRefreshLayout.setRefreshing(false);
            SparkleHelper.makeSnackbar(view, getString(R.string.rate_limit_error));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        // Refresh on resume
        startQueryIssueInfo(NO_JUMP);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save state
        super.onSaveInstanceState(savedInstanceState);
        if (issue != null)
        {
            savedInstanceState.putParcelable(ISSUE_DATA, issue);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore state
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            if (issue == null)
            {
                issue = savedInstanceState.getParcelable(ISSUE_DATA);
            }
        }
    }
}
