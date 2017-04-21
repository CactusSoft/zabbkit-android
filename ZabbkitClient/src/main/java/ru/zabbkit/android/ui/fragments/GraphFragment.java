package ru.zabbkit.android.ui.fragments;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.polites.android.GestureImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.db.entity.Bookmark;
import ru.zabbkit.android.db.entity.BookmarkEntity;
import ru.zabbkit.android.db.provider.DBProvider;
import ru.zabbkit.android.provider.TmpFileProvider;
import ru.zabbkit.android.ui.views.TimeIntervalView;
import ru.zabbkit.android.ui.views.TimePeriodRuler;
import ru.zabbkit.android.ui.views.assist.Period;
import ru.zabbkit.android.utils.Communicator;
import ru.zabbkit.android.utils.L;
import ru.zabbkit.android.utils.SharedPreferencesEditor;
import ru.zabbkitserver.android.remote.request.AsyncRequestListener;

/**
 * Created by Sergey.Tarasevich on 20.08.13.
 */
public class GraphFragment extends BaseListFragment implements
        TimeIntervalView.OnTimeIntervalChangedListener,
        TimePeriodRuler.OnPeriodChangedListener,
        AsyncRequestListener {

    public static final String TAG = GraphFragment.class.getSimpleName();
    public static final int ONE_SECOND = 1000;
    public static final int READ_TIMEOUT = 10 * ONE_SECOND;
    public static final int CONNECT_TIMEOUT = 15 * ONE_SECOND;
    public static final int INACTIVITY_TIME_INTERVAL = 5 * ONE_SECOND;
    public static final int BASE_PERIOD = 3600;
    private int period = BASE_PERIOD;
    public static final int JPEG_FULL_QUALITY = 100;
    private static final String FILE_NAME_TIME_FORMAT = "yyyyMMdd_HHmmss";
    private static final String FILE_NAME_PREFIX = ".jpg";
    static SSLContext sslContext = null;
    private final String graphRequestQuery = SharedPreferencesEditor
            .getInstance().getString(Constants.PREFS_URL_SHORTCUT)
            + Constants.GRAPH_REQ_QUERY;
    private RelativeLayout mTopPanel;
    private TimePeriodRuler mPeriodView;
    private TimeIntervalView mTimeIntervalView;
    private ImageView mImageView;
    private String mGraphId;
    private long stime;
    private int mLastSelectedPeriod;
    private BookmarkEntity bookmarkEntity;
    private boolean isGraphStored;
    private IdleTimer mIdleTimer;
    private Bitmap mGraphBitmap;

    private final int ID_MENU_SHARE = 1;
    private final int ID_MENU_SAVE = 2;
    private final int ID_MENU_BOOKMARK = 3;

    public static GraphFragment newInstance() {
        GraphFragment f = new GraphFragment();

        Bundle args = new Bundle();
        f.setArguments(args);

        return f;
    }

    private long getThisMomentTime() {
        return System.currentTimeMillis() / ONE_SECOND;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.d("onCreate");
        setRetainInstance(true);
        setHasOptionsMenu(true);

        initCookies();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        L.d("onCreateView");
        final View rootView = inflater.inflate(R.layout.fr_graph, container,
                false);

        mTopPanel = (RelativeLayout) rootView.findViewById(R.id.top_panel);
        mPeriodView = (TimePeriodRuler) rootView.findViewById(R.id.periods);
        mTimeIntervalView = (TimeIntervalView) rootView
                .findViewById(R.id.date_time_interval);
        mImageView = (GestureImageView) rootView.findViewById(R.id.img_graph);
        mImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                L.d("Graph onClick", "");
                togglePanels();
            }
        });
        return rootView;
    }

    @Override
    protected void sendRequest() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().overridePendingTransition(R.anim.slide_in,
                R.anim.slide_out);
        mGraphId = getArguments().getString(Constants.GRAPH_ID);
        if (mGraphBitmap == null && mImageView.getDrawable() == null) {
            L.i("mGraphBitmap == null");
            if (0 < getArguments().getInt(Constants.PERIOD)) {
                L.i("Period - " + getArguments().getInt(Constants.PERIOD));
                mPeriodView.setSelection(getArguments()
                        .getInt(Constants.PERIOD));
            } else {
                stime = getThisMomentTime() - period;
                final String url = makeURL();
                Communicator.getInstance().getGraph(this, url);
            }
        } else {
            mImageView.setImageBitmap(mGraphBitmap);
        }
        if (bookmarkEntity == null) {
            prepareBookMark();
        }
        checkIfGraphStored();
        initPanelsTimer();
    }

    @Override
    public void onStart() {
        super.onStart();
        L.d("onStart");
        mPeriodView.setOnPeriodChangedListener(this);
        mTimeIntervalView.setOnTimeIntervalChangedListener(this);
    }

    @Override
    public void onStop() {
        L.d("onStop");
        mPeriodView.removeOnPeriodChangedListener(this);
        mTimeIntervalView.removeOnIntervalChangedListener(this);
        mIdleTimer.stopIdleTimer();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem shareMenuItem = menu.add(0, ID_MENU_SHARE, 0, R.string.menu_share).setIcon(R.drawable.ic_menu_share);
        MenuItem saveMenuItem = menu.add(0, ID_MENU_SAVE, 0, R.string.menu_save).setIcon(android.R.drawable.ic_menu_save);
        MenuItem bookmarkMenuItem = menu.add(0, ID_MENU_BOOKMARK, 0, R.string.menu_bookmark).setIcon(android.R.drawable.star_big_off);
        MenuItemCompat.setShowAsAction(shareMenuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        MenuItemCompat.setShowAsAction(saveMenuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        MenuItemCompat.setShowAsAction(bookmarkMenuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem bookmarkMenuItem = menu.findItem(ID_MENU_BOOKMARK);
        if (isGraphStored) {
            bookmarkMenuItem.setIcon(android.R.drawable.star_big_on);
        } else {
            bookmarkMenuItem.setIcon(android.R.drawable.star_big_off);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ID_MENU_SHARE:
                SendGraphTask sendGraphTask = new SendGraphTask();
                sendGraphTask.execute();
                break;
            case ID_MENU_SAVE:
                SaveGraphTask saveGraphTask = new SaveGraphTask();
                saveGraphTask.execute();
                break;
            case ID_MENU_BOOKMARK:
                if (isGraphStored) {
                    removeGraphFromBookmarks();
                } else {
                    saveGraphToBookmarks();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onPeriodChanged(Period selectedPeriod) {
        Log.d(TAG, "onPeriodChanged()");
        if (mLastSelectedPeriod != mPeriodView.getSelectedItemPosition()) {
            mLastSelectedPeriod = mPeriodView.getSelectedItemPosition();
            mTimeIntervalView.setPeriodAndUpdateInterval(selectedPeriod);
            bookmarkEntity.setPeriod(mPeriodView.getSelectedItemPosition());
            checkIfGraphStored();
        }
    }

    @Override
    public void onTimeIntervalChanged(Period selectedPeriod,
                                      Calendar startTime, Calendar endTime) {
        Log.d(TAG, "onTimeIntervalChanged()");
        period = (int) ((endTime.getTimeInMillis() - startTime
                .getTimeInMillis()) / ONE_SECOND);
        if (period < BASE_PERIOD) {
            period = BASE_PERIOD;
        }
        stime = startTime.getTimeInMillis() / ONE_SECOND;
        final String url = makeURL();
        //new GraphTask().execute(url);
        Communicator.getInstance().getGraph(this, url);
        if (mIdleTimer != null) {
            mIdleTimer.restartIdleTimer();
        }
    }

    private void prepareBookMark() {
        final String hostId = getArguments().getString(Constants.HOST_ID);
        final String hostName = getArguments().getString(Constants.HOST_NAME);
        final String paramName = getArguments().getString(Constants.PARAM_NAME);
        bookmarkEntity = new BookmarkEntity();
        bookmarkEntity.setGraphId(mGraphId);
        bookmarkEntity.setServerId(hostId);
        bookmarkEntity.setServerName(hostName);
        bookmarkEntity.setParamName(paramName);
        bookmarkEntity.setPeriod(mPeriodView.getSelectedItemPosition());
    }

    private void checkIfGraphStored() {
        getActivity().getSupportLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String selectionQuery = DBProvider.UNIQUE_BOOKMARK_QUERY;
                String[] selectionQueryArgs = new String[]{
                        bookmarkEntity.getGraphId(),
                        String.valueOf(bookmarkEntity.getPeriod())};
                return new CursorLoader(getActivity(),
                        Uri.parse(DBProvider.BOOKMARKS_CONTENT_URI),
                        null, selectionQuery, selectionQueryArgs, null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                if (cursor.getCount() > 0) {
                    isGraphStored = true;
                } else {
                    isGraphStored = false;
                }
                getActivity().supportInvalidateOptionsMenu();
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });
    }

    private void saveGraphToBookmarks() {
        ContentValues values = new ContentValues();
        values.put(Bookmark.COLUMN_PERIOD, bookmarkEntity.getPeriod());
        values.put(Bookmark.COLUMN_SERVER_ID, bookmarkEntity.getServerId());
        values.put(Bookmark.COLUMN_GRAPH_ID, bookmarkEntity.getGraphId());
        values.put(Bookmark.COLUMN_PARAM_NAME, bookmarkEntity.getParamName());
        values.put(Bookmark.COLUMN_SERVER_NAME, bookmarkEntity.getServerName());
        getActivity().getContentResolver().insert(Uri.parse(DBProvider.BOOKMARKS_CONTENT_URI),
                values);
        checkIfGraphStored();
    }

    private void removeGraphFromBookmarks() {
        String selectionQuery = DBProvider.UNIQUE_BOOKMARK_QUERY;
        String[] selectionQueryArgs = new String[]{
                bookmarkEntity.getGraphId(),
                String.valueOf(bookmarkEntity.getPeriod())};
        getActivity().getContentResolver().delete(Uri.parse(DBProvider.BOOKMARKS_CONTENT_URI),
                selectionQuery, selectionQueryArgs);
        checkIfGraphStored();
    }

    private boolean isPanelsShown() {
        return (mTopPanel != null)
                && (mTopPanel.getVisibility() == View.VISIBLE);
    }

    private void initCookies() {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        if (cookieManager.getCookieStore().getCookies().size() == 0) {
            try {
                String url = SharedPreferencesEditor.getInstance().getString(
                        Constants.PREFS_URL_SHORTCUT);
                URI uri = new URI(url);
                HttpCookie cookie = new HttpCookie(Constants.COOKIE_AUTH_NAME,
                        SharedPreferencesEditor.getInstance().getString(
                                Constants.PREFS_AUTH));
                cookie.setDomain(uri.getHost());
                cookie.setPath(Constants.COOKIE_BASE_PATH);
                cookie.setVersion(0);
                cookie.setMaxAge((new Date()).getTime()
                        + Constants.COOKIE_LIFETIME_YEAR);
                cookieManager.getCookieStore().add(uri, cookie);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private String makeURL() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
        } else { // Older device
            size.x = display.getWidth();
            size.y = display.getHeight();
        }
        getResources().getConfiguration();
        int width = size.x;
        int height = size.y;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // To request landscape resolution
            width = size.y;
            height = size.x;
        }
        final String url = String.format(graphRequestQuery, mGraphId, width,
                height, stime, period);
        L.i(url);
        L.i(DateFormat.getDateTimeInstance().format(
                new Date(stime * ONE_SECOND)));
        L.i("Period = " + period);
        return url;
    }

    private void togglePanels() {
        if (isPanelsShown()) {
            hidePanels();
        } else {
            showPanels();
        }
    }

    private void showPanels() {
        mTopPanel.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                R.anim.top_graph_panel_slide_in));
        mTimeIntervalView.startAnimation(AnimationUtils.loadAnimation(
                getActivity(), R.anim.bottom_graph_panel_slide_in));
        mTopPanel.setVisibility(View.VISIBLE);
        mTimeIntervalView.setVisibility(View.VISIBLE);
        if (mIdleTimer != null) {
            mIdleTimer.restartIdleTimer();
        }
    }

    private void hidePanels() {
        mTopPanel.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                R.anim.top_graph_panel_slide_out));
        mTimeIntervalView.startAnimation(AnimationUtils.loadAnimation(
                getActivity(), R.anim.bottom_graph_panel_slide_out));
        mTopPanel.setVisibility(View.GONE);
        mTimeIntervalView.setVisibility(View.GONE);
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void saveImage(Bitmap bitmap, File imageFile) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_FULL_QUALITY, out);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initPanelsTimer() {
        mIdleTimer = new IdleTimer(INACTIVITY_TIME_INTERVAL,
                new IIdleCallback() {
                    @Override
                    public void inactivityDetected() {
                        L.i("inactivityDetected - hide panels");
                        // code runs in a UI thread
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isPanelsShown()) {
                                    hidePanels();
                                }
                            }
                        });
                    }
                });
    }

    public void trustAllHosts(HttpsURLConnection urlConnection) {
        try {
            if (sslContext == null) {
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }
                }
                };

                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            }
            urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestFailure(Exception e, String message) {

    }

    @Override
    public void onRequestSuccess(List<Object> result, Class<?> clazz) {
        if (result != null && result.size() > 0) {
            Bitmap graphBitmap = (Bitmap) result.get(0);
            mGraphBitmap = graphBitmap;
            mImageView.setImageBitmap(graphBitmap);
        }
    }

    @Override
    public void onCertificateRequest(X509Certificate[] certificate) {

    }

    @Override
    public void setObsoleteDataFlag() {

    }

    @Override
    public void updateDataSet() {

    }

    protected interface IIdleCallback {
        void inactivityDetected();
    }

    private static class IdleTimer {
        private final IIdleCallback idleCallback;
        private boolean isTimerRunning;
        private int maxIdleTime;
        private Timer timer;

        public IdleTimer(int maxInactivityTime, IIdleCallback callback) {
            maxIdleTime = maxInactivityTime;
            idleCallback = callback;
            isTimerRunning = false;
        }

        /*
         * creates new timer with idleTimer params and schedules a task
         */
        public void startIdleTimer() {
            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    idleCallback.inactivityDetected();
                }
            }, maxIdleTime);
            isTimerRunning = true;
        }

        /*
         * schedules new idle timer, call this to reset timer
         */
        public void restartIdleTimer() {
            if (isTimerRunning) {
                stopIdleTimer();
            }
            startIdleTimer();
        }

        /*
         * stops idle timer, canceling all scheduled tasks in it
         */
        public void stopIdleTimer() {
            if (isTimerRunning) {
                timer.cancel();
                isTimerRunning = false;
            }
        }

    }

    private class SaveGraphTask extends AsyncTask<Void, Void, Void> {

        boolean success;

        @Override
        protected void onPostExecute(Void result) {
            if (success) {
                showToast(getString(R.string.graph_saved));
            } else {
                showToast(getString(R.string.graph_not_saved));
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            saveImageFile(mGraphBitmap);
            return null;
        }

        private void saveImageFile(Bitmap bitmap) {
            String timeStamp = new SimpleDateFormat(FILE_NAME_TIME_FORMAT,
                    Locale.ENGLISH).format(new Date());
            if (bitmap != null) {
                String imageFileName = "ZabbKitGraph_" + timeStamp
                        + FILE_NAME_PREFIX;
                File storageDir = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File picturesDir = new File(storageDir,
                        Constants.ZABBKIT_PICTURES_DIR);
                if (!picturesDir.exists()) {
                    if (!picturesDir.mkdirs()) {
                        return;
                    }
                }
                File imageFile = new File(picturesDir, imageFileName);
                saveImage(bitmap, imageFile);
                galleryAddPic(imageFile.getAbsolutePath());
            }
        }

        private void galleryAddPic(String imageFilePath) {
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File file = new File(imageFilePath);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            getActivity().sendBroadcast(mediaScanIntent);
            success = true;
        }
    }

    private class SendGraphTask extends AsyncTask<Void, Void, Void> {

        boolean success;
        String imageFileName;

        @Override
        protected void onPostExecute(Void result) {
            if (success) {
                Uri imageUri = Uri.parse("content://"
                        + TmpFileProvider.AUTHORITY + "/" + imageFileName);
                final Intent emailIntent = new Intent(
                        android.content.Intent.ACTION_SEND);
                emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.share_graph_subject));
                emailIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                emailIntent.setType("image/png");
                startActivity(Intent.createChooser(emailIntent,
                        "Send email using"));
            } else {
                showToast(getString(R.string.graph_not_send));
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            saveTmpImageFile(mGraphBitmap);
            return null;
        }

        private void saveTmpImageFile(Bitmap bitmap) {
            String timeStamp = new SimpleDateFormat(FILE_NAME_TIME_FORMAT,
                    Locale.ENGLISH).format(new Date());
            if (bitmap != null) {
                imageFileName = timeStamp + FILE_NAME_PREFIX;

                File storageDir = getActivity().getCacheDir();
                File imageFile = new File(storageDir, imageFileName);
                saveImage(bitmap, imageFile);
                success = true;
            }
        }
    }

}
