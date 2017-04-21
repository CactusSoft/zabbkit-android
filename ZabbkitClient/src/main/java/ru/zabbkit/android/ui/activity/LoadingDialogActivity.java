package ru.zabbkit.android.ui.activity;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

import ru.zabbkit.android.ui.dialog.LoadingDialog;

/**
 * Created by Alex.Shimborsky on 10/12/2014.
 */
public abstract class LoadingDialogActivity extends ActionBarActivity {

    protected void showDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(LoadingDialog.DIALOG_TAG);
        if (prev == null) {
            ft.addToBackStack(null);
            DialogFragment newFragment = LoadingDialog.newInstance();
            newFragment.show(ft, LoadingDialog.DIALOG_TAG);
        }
    }

    protected boolean dismissDialog() {
        boolean isVisible = false;
        Fragment prev = getSupportFragmentManager().findFragmentByTag(LoadingDialog.DIALOG_TAG);
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismiss();
            isVisible = true;
        }
        return isVisible;
    }
}
