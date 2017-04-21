package ru.zabbkit.android.ui.activity;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.db.entity.Host;
import ru.zabbkit.android.db.provider.DBProvider;
import ru.zabbkit.android.ui.dialog.DialogHelper;
import ru.zabbkit.android.utils.Communicator;
import ru.zabbkit.android.utils.StringUtils;
import ru.zabbkitserver.android.remote.request.AsyncRequestListener;

/**
 * Created by Alex.Shimborsky on 20/10/2014.
 */
public class AddHostActivity extends LoadingDialogActivity implements View.OnClickListener, AsyncRequestListener,
        DialogHelper.HttpAuthListener, DialogHelper.AddCertificateListener {

    private EditText editUrl;
    private EditText editName;
    private EditText editLogin;
    private EditText editPass;
    private CheckBox checkSsl;
    private Button btnAdd;

    private boolean isNameChanged = false;

    private final String DIALOG_STATE = "dialogState";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_add_host);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_logo);

        editUrl = (EditText) findViewById(R.id.edit_host_url);
        editName = (EditText) findViewById(R.id.edit_host_name);
        editLogin = (EditText) findViewById(R.id.edit_host_login);
        editPass = (EditText) findViewById(R.id.edit_host_password);
        checkSsl = (CheckBox) findViewById(R.id.check_ssl);
        btnAdd = (Button) findViewById(R.id.btn_host_add);

        btnAdd.setOnClickListener(this);

        editUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (!isNameChanged) {
                    editName.setText(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        editUrl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    boolean currentIsNameChanged = isNameChanged;
                    isNameChanged = true;
                    editUrl.setText(StringUtils.checkURL(editUrl.getText().toString()));
                    isNameChanged = currentIsNameChanged;
                }
            }
        });

        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (editName.isFocused()) {
                    isNameChanged = true;
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_host_add:
                if (editUrl.getText().toString().length() > 0 &&
                        editName.getText().toString().length() > 0 &&
                        editLogin.getText().toString().length() > 0) {
                    if (URLUtil.isValidUrl(editUrl.getText().toString())) {
                        performAdd();
                    } else {
                        Toast.makeText(this, getString(R.string.url_invalid),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.fill_fields),
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void performAdd() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editUrl.getWindowToken(), 0);

        showDialog();
        final Map<String, Object> params = new ArrayMap<String, Object>();
        params.put(Constants.PREFS_USER, editLogin.getText().toString());
        params.put(Constants.PREFS_PASSWORD, editPass.getText().toString());
        Communicator.getInstance().login(params,
                collectUrl(editUrl.getText().toString()), this);
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
    public void onRequestFailure(Exception e, String message) {
        if (dismissDialog()) {
            final String msg = message;
            if (e == null) {
                DialogHelper.showAlertDialog(AddHostActivity.this, msg);
            } else if (e.getMessage().equals(Constants.HTTP_AUTH_FAIL) && message.equals("401")) {
                DialogHelper.showHTTPAuthDialog(AddHostActivity.this, AddHostActivity.this);
            } else {
                DialogHelper.showAlertDialog(AddHostActivity.this, msg);
            }
        }
    }

    @Override
    public void onRequestSuccess(List<Object> result, Class<?> clazz) {
        if (dismissDialog()) {
            int sslTrust = 0;
            if (checkSsl.isChecked()) {
                sslTrust = 1;
            }
            ContentValues values = new ContentValues();
            values.put(Host.COLUMN_SSL, sslTrust);
            values.put(Host.COLUMN_URL, editUrl.getText().toString());
            values.put(Host.COLUMN_LOGIN, editLogin.getText().toString());
            values.put(Host.COLUMN_PASSWORD, editPass.getText().toString());
            values.put(Host.COLUMN_NAME, editName.getText().toString());
            getContentResolver().insert(Uri.parse(DBProvider.HOSTS_CONTENT_URI),
                    values);
            finish();
        }
    }

    @Override
    public void onCertificateRequest(final X509Certificate[] certificate) {
        if (dismissDialog()) {
            if (certificate == null) {
                performAdd();
            } else {
                DialogHelper.showSslDialog(AddHostActivity.this,
                        certificate, AddHostActivity.this);
            }
        }
    }

    @Override
    public void resumeRequest(boolean answer) {
        if (answer) {
            performAdd();
        }
    }
}
