package ru.zabbkit.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.app.ZabbkitApplication;
import ru.zabbkit.android.ui.dialog.DialogHelper;
import ru.zabbkit.android.ui.dialog.DialogHelper.AddCertificateListener;
import ru.zabbkit.android.ui.dialog.DialogHelper.HttpAuthListener;
import ru.zabbkit.android.utils.Communicator;
import ru.zabbkit.android.utils.L;
import ru.zabbkit.android.utils.LoginRequestListener;
import ru.zabbkit.android.utils.NetworkUtils;
import ru.zabbkit.android.utils.SharedPreferencesEditor;
import ru.zabbkit.android.utils.StringUtils;

public class LoginActivity extends LoadingDialogActivity implements
        LoginRequestListener, OnClickListener, OnEditorActionListener,
        AddCertificateListener, HttpAuthListener {

    private EditText mNameView;
    private EditText mPasswordView;
    private EditText mUrlView;
    private ImageView mHostsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_login);

        boolean isUserAuthorized = SharedPreferencesEditor.getInstance()
                .getBoolean(Constants.PREFS_IS_AUTHORIZED, false);
        if (isUserAuthorized) {
            finish();
            Intent intent = new Intent(getApplicationContext(),
                    SlideMenuActivity.class);
            startActivity(intent);
        } else {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.view_logo);

            mHostsView = (ImageView) findViewById(R.id.image_hosts);
            mHostsView.setOnClickListener(this);
            mUrlView = (EditText) findViewById(R.id.edit_address);
            mUrlView.setText(SharedPreferencesEditor.getInstance().getString(
                    Constants.PREFS_URL_SHORTCUT));
            mUrlView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b) {
                        mUrlView.setText(StringUtils.checkURL(mUrlView.getText().toString()));
                    }
                }
            });

            mNameView = (EditText) findViewById(R.id.edit_name);
            mNameView.setText(SharedPreferencesEditor.getInstance().getString(
                    Constants.PREFS_USER));
            mPasswordView = (EditText) findViewById(R.id.edit_pass);
            final Button loginBtn = (Button) findViewById(R.id.btn_login);
            loginBtn.setOnClickListener(this);
            mPasswordView.setOnEditorActionListener(this);

            FlurryAgent.logEvent("Application started");

            fillFromBundle(getIntent());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, Constants.FLURRY_APP_KEY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    @Override
    public void onBackPressed() {
        if (!dismissDialog()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestFailure(Exception e, String message) {
        if (dismissDialog()) {
            final String msg = message;
            if (e == null) {
                DialogHelper.showAlertDialog(LoginActivity.this, msg);
            } else if (e.getMessage().equals(Constants.HTTP_AUTH_FAIL) && message.equals("401")) {
                DialogHelper.showHTTPAuthDialog(LoginActivity.this, LoginActivity.this);
            } else {
                DialogHelper.showAlertDialog(LoginActivity.this, msg);
            }
        }
    }

    @Override
    public void onRequestSuccess(List<Object> result, Class<?> clazz) {
        onRequestSuccess(null, result, clazz);
    }

    @Override
    public void onRequestSuccess(String redirectedUrl, List<Object> result, Class<?> clazz) {
        if (dismissDialog()) {
            String url = null;
            if (redirectedUrl == null) {
                url = mUrlView.getText().toString();
            } else {
                url = redirectedUrl;
            }
            url = url.trim();
            SharedPreferencesEditor.getInstance().putString(
                    Constants.PREFS_URL_SHORTCUT, url);
            SharedPreferencesEditor.getInstance().putString(
                    Constants.PREFS_URL_FULL, collectUrl(url));
            SharedPreferencesEditor.getInstance().putString(Constants.PREFS_AUTH,
                    (String) result.get(0));
            SharedPreferencesEditor.getInstance().putString(Constants.PREFS_USER,
                    mNameView.getText().toString().trim());
            SharedPreferencesEditor.getInstance().putBoolean(
                    Constants.PREFS_IS_AUTHORIZED, true);
            SharedPreferencesEditor.getInstance().putString(
                    Constants.PREFS_PASSWORD, mPasswordView.getText().toString());
            FlurryAgent.logEvent("User was sign in successfully");
            final Intent intent = new Intent(getApplicationContext(),
                    SlideMenuActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mUrlView.getWindowToken(), 0);
                if (NetworkUtils.isNetEnabled(LoginActivity.this)) {
                    performLogin();
                }
                break;
            case R.id.image_hosts:
                final Intent intent = new Intent(getApplicationContext(),
                        HostActivity.class);
                startActivityForResult(intent, Constants.HOST_SELECT_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.HOST_SELECT_CODE:
                    fillFromBundle(data);
                    break;
                case Constants.LOGIN_CHANGE_CODE:
                    fillFromBundle(data);
                    performLogin();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void fillFromBundle(Intent data) {
        mUrlView.setText(data.getStringExtra(Constants.HOST_BUNDLE_URL));
        mNameView.setText(data.getStringExtra(Constants.HOST_BUNDLE_LOGIN));
        mPasswordView.setText(data.getStringExtra(Constants.HOST_BUNDLE_PASSWORD));
        boolean isAutoLogin = data.getBooleanExtra(Constants.AUTO_LOGIN, false);
        if (isAutoLogin) {
            performLogin();
        }
    }

    private void performLogin() {
        if (!TextUtils.isEmpty(mNameView.getText().toString())
                && !TextUtils.isEmpty(mPasswordView.getText().toString())
                && !TextUtils.isEmpty(mUrlView.getText().toString())) {
            final String userName = mNameView.getText().toString().trim();
            final String userPassword = mPasswordView.getText().toString();
            final Map<String, Object> params = new ArrayMap<String, Object>();
            params.put(Constants.PREFS_USER, userName);
            params.put(Constants.PREFS_PASSWORD, userPassword);

            showDialog();
            Communicator.getInstance().login(params,
                    collectUrl(mUrlView.getText().toString()), this);
        } else {
            DialogHelper.showAlertDialog(LoginActivity.this, getString(R.string.fill_fields));
        }
    }

    private String collectUrl(String url) {
        String resultUrl = url.trim();
        if (resultUrl.length() > 0
                && resultUrl.charAt(resultUrl.length() - 1) != Constants.HTTP_SYMBOL) {
            resultUrl += Constants.HTTP_SYMBOL;
        }
        resultUrl += Constants.API_ADDRESS;

        return resultUrl;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                || (actionId == EditorInfo.IME_ACTION_DONE)) {
            L.i("Enter pressed");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            performLogin();
        }
        return true;
    }

    @Override
    public void onCertificateRequest(final X509Certificate[] certificate) {
        dismissDialog();
        if (certificate == null) {
            performLogin();
        } else {
            DialogHelper.showSslDialog(LoginActivity.this,
                    certificate, LoginActivity.this);
        }

    }

    @Override
    public void resumeRequest(boolean answer) {
        if (answer) {
            performLogin();
        }
    }

}
