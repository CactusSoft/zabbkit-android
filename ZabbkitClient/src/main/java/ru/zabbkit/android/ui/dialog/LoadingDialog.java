package ru.zabbkit.android.ui.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import ru.zabbkit.android.R;

/**
 * Created by Alex.Shimborsky on 10/12/2014.
 */
public class LoadingDialog extends DialogFragment {

    public static final String DIALOG_TAG = "dialog";

    public static LoadingDialog newInstance() {
        LoadingDialog dialog = new LoadingDialog();
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog mProgressDialog = ProgressDialog.show(getActivity(), "",
                getString(R.string.loading), true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        return mProgressDialog;
    }

}
