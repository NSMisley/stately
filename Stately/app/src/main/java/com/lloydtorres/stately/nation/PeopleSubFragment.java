package com.lloydtorres.stately.nation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.MortalityCause;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-12.
 * A sub-fragment of the Nation fragment showing data on people.
 * Takes in a Nation object.
 */
public class PeopleSubFragment extends Fragment {
    private Nation mNation;

    private TextView summaryDesc;
    private PieChart mortalityChart;

    // Labels on the mortality chart
    private List<String> chartLabels;

    public void setNation(Nation n)
    {
        mNation = n;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub_people, container, false);

        // Restore state
        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable("mNation");
        }

        if (mNation != null)
        {
            initSummaryDesc(view);
            initMortalityChart(view);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        if (mNation != null)
        {
            outState.putParcelable("mNation", mNation);
        }
    }

    /**
     * Initialize the first card showing a mockup of the people descriptors in NationStates.
     * @param view
     */
    private void initSummaryDesc(View view)
    {
        summaryDesc = (TextView) view.findViewById(R.id.nation_summarydesc);

        String summaryContent = String.format(getString(R.string.card_people_summarydesc_flavour),
                mNation.prename,
                mNation.name,
                mNation.notable,
                mNation.sensible,
                SparkleHelper.getPopulationFormatted(getContext(), mNation.popBase),
                mNation.demPlural);

        summaryContent += "<br /><br />" + mNation.crime;

        summaryDesc.setText(SparkleHelper.getHtmlFormatting(summaryContent));
    }

    /**
     * Initialize the mortality pie chart.
     * @param view
     */
    private void initMortalityChart(View view)
    {
        mortalityChart = (PieChart) view.findViewById(R.id.nation_mortality_chart);

        // setup data
        chartLabels = new ArrayList<String>();
        List<Entry> chartEntries = new ArrayList<Entry>();
        List<MortalityCause> causes = mNation.mortalityRoot.causes;

        for (int i=0; i < causes.size(); i++)
        {
            // NationStates API stores this as Animal Attack instead of
            // using the actual national animal, so replace that
            if (getString(R.string.animal_attack_original).equals(causes.get(i).type))
            {
                chartLabels.add(String.format(getString(R.string.animal_attack_madlibs), mNation.animal));
            }
            else
            {
                chartLabels.add(causes.get(i).type);
            }
            Entry n = new Entry((float) causes.get(i).value, i);
            chartEntries.add(n);
        }

        // Disable labels, set values and colours
        PieDataSet dataSet = new PieDataSet(chartEntries, "");
        dataSet.setDrawValues(false);
        dataSet.setColors(SparkleHelper.chartColours, getActivity());
        PieData dataFull = new PieData(chartLabels, dataSet);

        mortalityChart = SparkleHelper.getFormattedPieChart(getContext(), mortalityChart, chartLabels);
        mortalityChart.setData(dataFull);
        mortalityChart.invalidate();
    }
}
