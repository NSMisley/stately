package com.lloydtorres.stately.login;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.UserLogin;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Lloyd on 2016-02-03.
 * This dialog is shown for switching active nations.
 */
public class SwitchNationDialog extends DialogFragment {
    public static final String DIALOG_TAG = "fragment_switch_dialog";
    public static final String LOGINS_KEY = "logins";

    // RecyclerView variables
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    private ArrayList<UserLogin> logins;

    public void setLogins(ArrayList<UserLogin> l)
    {
        logins = l;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.AlertDialogCustom);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_paddedrecycler, container, false);
        getDialog().setTitle(getString(R.string.menu_switch));
        getDialog().setCanceledOnTouchOutside(true);

        // Restore saved state
        if (savedInstanceState != null)
        {
            logins = savedInstanceState.getParcelableArrayList(LOGINS_KEY);
        }

        initRecycler(view);

        return view;
    }

    private void initRecycler(View view)
    {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_padded);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        Collections.sort(logins);
        mRecyclerAdapter = new SwitchNationRecyclerAdapter(getContext(), this, logins);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save state
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(LOGINS_KEY, logins);
    }
}
