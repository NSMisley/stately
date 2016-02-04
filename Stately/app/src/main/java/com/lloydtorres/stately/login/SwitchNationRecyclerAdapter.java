package com.lloydtorres.stately.login;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.UserLogin;

import java.util.List;

/**
 * Created by Lloyd on 2016-02-03.
 * This is the recycler adapter used for SwitchNationDialog.
 */
public class SwitchNationRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private SwitchNationDialog selfDialog;
    private List<UserLogin> logins;

    public SwitchNationRecyclerAdapter(Context c, SwitchNationDialog d, List<UserLogin> u)
    {
        context = c;
        selfDialog = d;
        logins = u;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_name_basic, parent, false);
        RecyclerView.ViewHolder viewHolder = new SwitchNationEntry(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SwitchNationEntry switchNationEntry = (SwitchNationEntry) holder;
        switchNationEntry.init(logins.get(position));
    }

    @Override
    public int getItemCount() {
        return logins.size();
    }

    public class SwitchNationEntry extends RecyclerView.ViewHolder implements View.OnClickListener {
        private UserLogin login;
        private TextView nationName;

        public SwitchNationEntry(View v) {
            super(v);
            nationName = (TextView) v.findViewById(R.id.basic_nation_name);
            v.setOnClickListener(this);
        }

        public void init(UserLogin u)
        {
            login = u;
            nationName.setText(login.name);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION)
            {
                Intent loginActivityLaunch = new Intent(context, LoginActivity.class);
                loginActivityLaunch.putExtra(LoginActivity.USERNAME_KEY, login.name);
                loginActivityLaunch.putExtra(LoginActivity.AUTOLOGIN_KEY, login.autologin);
                loginActivityLaunch.putExtra(LoginActivity.NOAUTOLOGIN_KEY, true);
                context.startActivity(loginActivityLaunch);
                selfDialog.dismiss();
            }
        }
    }
}
