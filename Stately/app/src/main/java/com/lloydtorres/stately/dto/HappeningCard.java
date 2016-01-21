package com.lloydtorres.stately.dto;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.helpers.SparkleHelper;

/**
 * Created by Lloyd on 2016-01-16.
 * A ViewHolder used to show a happenings card in a RecyclerView.
 */
public class HappeningCard extends RecyclerView.ViewHolder {

    private Context context;
    private TextView cardTime;
    private TextView cardContent;

    public HappeningCard(Context c, View v) {
        super(v);
        context = c;
        cardTime = (TextView) v.findViewById(R.id.card_happening_time);
        cardContent = (TextView) v.findViewById(R.id.card_happening_content);
    }

    public void init(HappeningEvent ev)
    {
        cardTime.setText(SparkleHelper.getReadableDateFromUTC(ev.timestamp));
        SparkleHelper.setHappeningsFormatting(context, cardContent, ev.content);
    }
}
