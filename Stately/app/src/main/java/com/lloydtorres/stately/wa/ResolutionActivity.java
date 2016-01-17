package com.lloydtorres.stately.wa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Assembly;
import com.lloydtorres.stately.dto.AssemblyActive;
import com.lloydtorres.stately.dto.Resolution;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-17.
 */
public class ResolutionActivity extends AppCompatActivity implements OnChartValueSelectedListener {
    private AssemblyActive mAssembly;
    private Resolution mResolution;
    private int councilId;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private TextView title;
    private TextView target;
    private TextView proposedBy;
    private TextView voteStart;
    private TextView votesFor;
    private TextView votesAgainst;

    private TextView content;

    private PieChart votingBreakdown;
    private LineChart votingHistory;
    private List<String> chartLabels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wa_council);

        if (getIntent() != null)
        {
            councilId = getIntent().getIntExtra("councilId", 1);
            mResolution = getIntent().getParcelableExtra("resolution");
        }
        if (savedInstanceState != null)
        {
            councilId = savedInstanceState.getInt("councilId");
            mResolution = savedInstanceState.getParcelable("resolution");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_wa_council);
        setToolbar(toolbar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.wa_resolution_refresher);
        mSwipeRefreshLayout.setColorSchemeResources(SparkleHelper.refreshColours);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryResolution(councilId);
            }
        });

        title = (TextView) findViewById(R.id.wa_resolution_title);
        target = (TextView) findViewById(R.id.wa_nominee);
        proposedBy = (TextView) findViewById(R.id.wa_proposed_by);
        voteStart = (TextView) findViewById(R.id.wa_activetime);
        votesFor = (TextView) findViewById(R.id.wa_resolution_for);
        votesAgainst = (TextView) findViewById(R.id.wa_resolution_against);

        content = (TextView) findViewById(R.id.wa_resolution_content);

        votingBreakdown = (PieChart) findViewById(R.id.wa_voting_breakdown);
        votingHistory = (LineChart) findViewById(R.id.wa_voting_history);

        if (mResolution == null)
        {
            // hack to get swiperefreshlayout to show
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
            queryResolution(councilId);
        }
        else
        {
            AssemblyActive tmp = new AssemblyActive();
            tmp.resolution = mResolution;
            setResolution(tmp);
        }
    }

    public void setToolbar(Toolbar t) {
        setSupportActionBar(t);
        getSupportActionBar().setElevation(0);

        switch (councilId)
        {
            case Assembly.GENERAL_ASSEMBLY:
                getSupportActionBar().setTitle(getString(R.string.wa_general_assembly));
                break;
            case Assembly.SECURITY_COUNCIL:
                getSupportActionBar().setTitle(getString(R.string.wa_security_council));
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void queryResolution(int chamberId)
    {
        final View fView = findViewById(R.id.wa_council_main);

        RequestQueue queue = Volley.newRequestQueue(this);
        String targetURL = String.format(AssemblyActive.QUERY, chamberId);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, targetURL,
                new Response.Listener<String>() {
                    AssemblyActive waResponse = null;
                    @Override
                    public void onResponse(String response) {
                        Persister serializer = new Persister();
                        try {
                            waResponse = serializer.read(AssemblyActive.class, response);
                            setResolution(waResponse);
                        }
                        catch (Exception e) {
                            SparkleHelper.logError(e.toString());
                            SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_parsing));
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SparkleHelper.logError(error.toString());
                mSwipeRefreshLayout.setRefreshing(false);
                if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                    SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_no_internet));
                }
                else
                {
                    SparkleHelper.makeSnackbar(fView, getString(R.string.login_error_generic));
                }
            }
        });

        queue.add(stringRequest);
    }

    private void setResolution(AssemblyActive res)
    {
        mAssembly = res;
        mResolution = mAssembly.resolution;

        title.setText(mResolution.name);
        target.setText(String.format(getString(R.string.wa_nominee_template), mResolution.category, mResolution.target));
        proposedBy.setText(String.format(getString(R.string.wa_proposed), mResolution.proposedBy));
        voteStart.setText(String.format(getString(R.string.wa_voting_time), SparkleHelper.getReadableDateFromUTC(mResolution.created)));
        votesFor.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesFor));
        votesAgainst.setText(SparkleHelper.getPrettifiedNumber(mResolution.votesAgainst));

        content.setText(SparkleHelper.getHtmlFormatting(mResolution.content));
        setVotingBreakdown(mResolution.votesFor, mResolution.votesAgainst);
        setVotingHistory(mResolution.voteHistoryFor, mResolution.voteHistoryAgainst);

        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void setVotingBreakdown(int voteFor, int voteAgainst)
    {
        float voteTotal = voteFor + voteAgainst;
        float votePercentFor = (((float) voteFor) * 100f)/voteTotal;
        float votePercentAgainst = (((float) voteAgainst) * 100f)/voteTotal;

        chartLabels = new ArrayList<String>();
        List<Entry> chartEntries = new ArrayList<Entry>();

        int i = 0;
        chartLabels.add(getString(R.string.wa_for));
        chartEntries.add(new Entry((float) votePercentFor, i++));
        chartLabels.add(getString(R.string.wa_against));
        chartEntries.add(new Entry((float) votePercentAgainst, i++));

        PieDataSet dataSet = new PieDataSet(chartEntries, "");
        dataSet.setDrawValues(false);
        dataSet.setColors(SparkleHelper.waColours, this);
        PieData dataFull = new PieData(chartLabels, dataSet);

        // formatting
        Legend cLegend = votingBreakdown.getLegend();
        cLegend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        cLegend.setForm(Legend.LegendForm.CIRCLE);
        cLegend.setTextSize(15);
        cLegend.setWordWrapEnabled(true);

        votingBreakdown.setDrawSliceText(false);
        votingBreakdown.setDescription("");
        votingBreakdown.setHoleColorTransparent(true);
        votingBreakdown.setHoleRadius(60f);
        votingBreakdown.setTransparentCircleRadius(65f);
        votingBreakdown.setCenterTextSize(20);
        votingBreakdown.setRotationEnabled(false);

        votingBreakdown.setOnChartValueSelectedListener(this);
        votingBreakdown.setData(dataFull);
        votingBreakdown.invalidate();
    }

    private void setVotingHistory(List<Integer> votesFor, List<Integer> votesAgainst)
    {
        List<Entry> entryFor = new ArrayList<Entry>();
        List<Entry> entryAgainst = new ArrayList<Entry>();

        for (int i=0; i < votesFor.size(); i++)
        {
            entryFor.add(new Entry(votesFor.get(i), i));
            entryAgainst.add(new Entry(votesAgainst.get(i), i));
        }

        LineDataSet setFor = new LineDataSet(entryFor, getString(R.string.wa_for));
        setFor.setAxisDependency(YAxis.AxisDependency.LEFT);
        setFor.setColors(SparkleHelper.waColourFor, this);
        setFor.setDrawValues(false);
        setFor.setDrawHighlightIndicators(false);
        setFor.setDrawCircles(false);
        setFor.setLineWidth(2.5f);

        LineDataSet setAgainst = new LineDataSet(entryAgainst, getString(R.string.wa_against));
        setAgainst.setAxisDependency(YAxis.AxisDependency.LEFT);
        setAgainst.setColors(SparkleHelper.waColourAgainst, this);
        setAgainst.setDrawValues(false);
        setAgainst.setDrawHighlightIndicators(false);
        setAgainst.setDrawCircles(false);
        setAgainst.setLineWidth(2.5f);

        List<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(setFor);
        dataSets.add(setAgainst);

        List<String> xLabels = new ArrayList<String>();
        for (int i=0; i < votesFor.size(); i++)
        {
            // Only add labels for each day
            if (i%24 == 0)
            {
                xLabels.add(String.format(getString(R.string.wa_x_axis_d), (i/24)+1));
            }
            else
            {
                xLabels.add(String.format(getString(R.string.wa_x_axis_h), i));
            }
        }
        LineData data = new LineData(xLabels, dataSets);

        // formatting
        Legend cLegend = votingHistory.getLegend();
        cLegend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        cLegend.setForm(Legend.LegendForm.CIRCLE);
        cLegend.setTextSize(15);
        cLegend.setWordWrapEnabled(true);

        XAxis xAxis = votingHistory.getXAxis();
        xAxis.setLabelsToSkip(23);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisRight = votingHistory.getAxisRight();
        yAxisRight.setEnabled(false);

        votingHistory.setDoubleTapToZoomEnabled(false);
        votingHistory.setDescription("");
        votingHistory.setScaleYEnabled(false);
        votingHistory.setDrawGridBackground(false);

        votingHistory.setData(data);
        votingHistory.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        if (votingBreakdown != null)
        {
            votingBreakdown.setCenterText(String.format(getString(R.string.chart_inner_text), chartLabels.get(e.getXIndex()), e.getVal()));
        }
    }

    @Override
    public void onNothingSelected() {
        if (votingBreakdown != null)
        {
            votingBreakdown.setCenterText("");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("councilId", councilId);
        if (mResolution != null)
        {
            savedInstanceState.putParcelable("resolution", mResolution);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            councilId = savedInstanceState.getInt("councilId");
            if (mResolution == null)
            {
                mResolution = savedInstanceState.getParcelable("resolution");
            }
        }
    }
}
