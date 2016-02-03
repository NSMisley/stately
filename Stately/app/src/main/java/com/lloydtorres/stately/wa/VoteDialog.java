package com.lloydtorres.stately.wa;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import com.lloydtorres.stately.R;

/**
 * Created by Lloyd on 2016-02-02.
 * This dialog displays voting options for a WA resolution.
 */
public class VoteDialog extends DialogFragment {
    public static final String DIALOG_TAG = "fragment_vote_dialog";
    public static final int VOTE_FOR = 0;
    public static final int VOTE_AGAINST = 1;
    public static final int VOTE_UNDECIDED = 2;

    private RadioGroup voteToggleState;
    private int choice;

    public VoteDialog() { }

    public void setChoice(int c)
    {
        choice = c;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)  {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_vote_dialog, null);

        voteToggleState = (RadioGroup) dialogView.findViewById(R.id.vote_radio_group);

        switch(choice)
        {
            case VOTE_FOR:
                voteToggleState.check(R.id.vote_radio_for);
                break;
            case VOTE_AGAINST:
                voteToggleState.check(R.id.vote_radio_against);
                break;
            default:
                voteToggleState.check(R.id.vote_radio_undecided);
                break;
        }

        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // @TODO
            }
        };

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(R.string.wa_vote_dialog_title)
                .setView(dialogView)
                .setPositiveButton(R.string.wa_vote_dialog_submit, dialogListener)
                .setNegativeButton(R.string.explore_negative, null);

        return dialogBuilder.create();
    }
}
