package ru.zabbkit.android.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.utils.SharedPreferencesEditor;
import ru.zabbkitserver.android.remote.utils.SSLManager;

/**
 * Created by Alex.Shimborsky on 14/11/2014.
 */
public class DialogHelper {

    private DialogHelper() {
    }

    public static void showAlertDialog(final Context ctx, String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx);

        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton("OK", null);

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    public static void showSslDialog(final Context ctx, final X509Certificate[] certificate,
                                     final AddCertificateListener listener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx);
        dialogBuilder.setTitle(R.string.certificate_not_trust);
        String hostname = ctx.getString(R.string.hostname_not_found);
        try {
            if (certificate[0].getSubjectAlternativeNames() != null) {
                hostname = certificate[0].getSubjectAlternativeNames().iterator().next().toString();
            }
        } catch (CertificateParsingException e) {
            // Do nothing
        }
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(ctx.getString(R.string.hostname));
        messageBuilder.append(hostname);
        messageBuilder.append("\n");
        messageBuilder.append(ctx.getString(R.string.subscriber));
        messageBuilder.append(certificate[0].getIssuerDN().toString());
        messageBuilder.append("\n");
        messageBuilder.append(ctx.getString(R.string.end_validity_period));
        messageBuilder.append(new SimpleDateFormat(Constants.DATE_FORMAT_CERTIFICATE).format(certificate[0].getNotAfter()));

        dialogBuilder.setMessage(messageBuilder.toString());
        dialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    public void run() {
                        SSLManager.getInstance().addCertificates(certificate);
                        ((Activity) ctx).runOnUiThread(new Runnable() {
                            public void run() {
                                listener.resumeRequest(true);
                            }
                        });
                    }
                }).start();
            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ctx, ctx.getString(R.string.certificate_not_added), Toast.LENGTH_LONG).show();
                listener.resumeRequest(false);
            }
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    public interface AddCertificateListener {
        void resumeRequest(boolean answer);
    }

    public static void showHTTPAuthDialog(final Context ctx, final HttpAuthListener httpAuthListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx);
        dialogBuilder.setTitle(R.string.http_basic_auth);

        LinearLayout logPassContainer = new LinearLayout(ctx);
        logPassContainer.setOrientation(LinearLayout.VERTICAL);
        final EditText loginEditText = new EditText(ctx);
        loginEditText.setHint("Login");
        final EditText passEditText = new EditText(ctx);
        passEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passEditText.setHint("Password");

        logPassContainer.addView(loginEditText);
        logPassContainer.addView(passEditText);
        dialogBuilder.setView(logPassContainer);

        dialogBuilder.setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String login = loginEditText.getText().toString().trim();
                String pass = passEditText.getText().toString().trim();
                if (login.length() > 0 && pass.length() > 0) {
                    StringBuilder pairBuilder = new StringBuilder();
                    pairBuilder.append(login);
                    pairBuilder.append(":");
                    pairBuilder.append(pass);

                    SharedPreferencesEditor.getInstance().putString(Constants.PREFS_HTTP_AUTH_LOGIN, login);
                    SharedPreferencesEditor.getInstance().putString(Constants.PREFS_HTTP_AUTH_PASS, pass);
                    httpAuthListener.resumeRequest(true);
                } else {
                    httpAuthListener.resumeRequest(false);
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                httpAuthListener.resumeRequest(false);
            }
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    public interface HttpAuthListener {
        void resumeRequest(boolean answer);
    }
}
