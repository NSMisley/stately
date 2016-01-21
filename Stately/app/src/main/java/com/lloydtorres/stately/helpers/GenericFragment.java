package com.lloydtorres.stately.helpers;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lloydtorres.stately.R;

/**
 * Created by Lloyd on 2016-01-14.
 * A generic empty fragment with toolbar that can be used as a placeholder.
 * Originally intended for use within a PrimeActivity, but feel free to use it elsewhere.
 */
public class GenericFragment extends Fragment {

    private Activity mActivity;
    private Toolbar toolbar;

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
        View view = inflater.inflate(R.layout.fragment_generic, container, false);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar_generic);

        if (mActivity instanceof PrimeActivity)
        {
            ((PrimeActivity) mActivity).setToolbar(toolbar);
        }

        return view;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mActivity = null;
    }

}
