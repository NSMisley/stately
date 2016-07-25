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

package com.lloydtorres.stately.nation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.census.TrendsActivity;
import com.lloydtorres.stately.dto.Nation;
import com.lloydtorres.stately.dto.NationChartCardData;
import com.lloydtorres.stately.dto.NationGenericCardData;
import com.lloydtorres.stately.helpers.SparkleHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lloyd on 2016-01-12.
 * A sub-fragment within the Nation fragment that displays government data.
 * Takes in a Nation object.
 */
public class GovernmentSubFragment extends Fragment {
    public static final String NATION_DATA_KEY = "mNation";

    private Nation mNation;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private List<Object> cards;

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
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);

        // Restore save state
        if (savedInstanceState != null && mNation == null)
        {
            mNation = savedInstanceState.getParcelable(NATION_DATA_KEY);
        }

        if (mNation != null)
        {
            mRecyclerView = (RecyclerView) view.findViewById(R.id.happenings_recycler);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);

            cards = new ArrayList<Object>();

            NationGenericCardData ngcSummary = new NationGenericCardData();
            ngcSummary.title = getString(R.string.card_main_title_summary);
            String descContent = mNation.govtDesc;
            descContent = descContent.replace(". ", ".<br /><br />");
            ngcSummary.mainContent = descContent;
            ngcSummary.nationCensusTarget = mNation.name;
            ngcSummary.idCensusTarget = TrendsActivity.CENSUS_GOVERNMENT_SIZE;
            cards.add(ngcSummary);

            NationChartCardData nccExpenditures = new NationChartCardData();
            nccExpenditures.details = new LinkedHashMap<String, String>();
            long budgetHolder = (long) (mNation.gdp * (mNation.sectors.government/100d));
            String budgetText = String.format(Locale.US, getString(R.string.card_government_expenditures_budget_flavour), SparkleHelper.getMoneyFormatted(getContext(), budgetHolder, mNation.currency), mNation.sectors.government);
            nccExpenditures.details.put(getString(R.string.card_government_expenditures_budget), budgetText);
            nccExpenditures.mode = NationChartCardData.MODE_GOV;
            nccExpenditures.govBudget = mNation.govBudget;
            cards.add(nccExpenditures);

            mRecyclerAdapter = new NationCardsRecyclerAdapter(getContext(), cards, getFragmentManager());
            mRecyclerView.setAdapter(mRecyclerAdapter);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNation != null)
        {
            outState.putParcelable(NATION_DATA_KEY, mNation);
        }
    }
}
