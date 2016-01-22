package com.lloydtorres.stately.helpers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lloydtorres.stately.R;

import java.util.List;

/**
 * Created by Lloyd on 2016-01-19.
 * An adapter used for the endorsement dialog's recycler.
 */
public class NameListRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private NameListDialog selfDialog;
    private List<String> names;
    private int target;

    public NameListRecyclerAdapter(Context c, NameListDialog d, List<String> n, int t)
    {
        context = c;
        selfDialog = d;
        names = n;
        target = t;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_name_basic, parent, false);
        RecyclerView.ViewHolder viewHolder = new EndorsementEntry(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        EndorsementEntry happeningCard = (EndorsementEntry) holder;
        happeningCard.init(names.get(position));
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public class EndorsementEntry extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView nationName;

        public EndorsementEntry(View v) {
            super(v);
            nationName = (TextView) v.findViewById(R.id.basic_nation_name);
            v.setOnClickListener(this);
        }

        public void init(String n)
        {
            nationName.setText(n);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION)
            {
                switch (target)
                {
                    case SparkleHelper.CLICKY_NATION_MODE:
                        // Start exploring nation and close dialog on click.
                        SparkleHelper.startExploring(context, names.get(pos));
                        selfDialog.dismiss();
                        break;
                }
            }
        }
    }
}