package co.uk.sentinelweb.androidgetdata;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Date;

import co.uk.sentinelweb.androidgetdata.util.DiskUtil;
import co.uk.sentinelweb.androidgetdata.util.DispUtil;
import co.uk.sentinelweb.androidgetdata.util.SendObjectParent;
import co.uk.sentinelweb.androidgetdata.util.StorageOptions;

public class MainActivity extends Activity {

    public static final double MM_IN_INCH = 25.4;
    public static final int PERM_REQUEST_CODE = 1111;
    private LinearLayout ctnr;

    public static final SparseArray<String> _orientation = new SparseArray<>();
    public static final SparseArray<String> _nav = new SparseArray<>();
    public static final SparseArray<String> _navHidden = new SparseArray<>();
    public static final SparseArray<String> _touchScreen = new SparseArray<>();
    public static final SparseArray<String> _screenSize = new SparseArray<>();
    public static final SparseArray<String> _screenLong = new SparseArray<>();
    public static final SparseArray<String> _screenLayoutDir = new SparseArray<>();

    static {
        _orientation.put(Configuration.ORIENTATION_LANDSCAPE, "ORIENTATION_LANDSCAPE");
        _orientation.put(Configuration.ORIENTATION_PORTRAIT, "ORIENTATION_PORTRAIT");
        _orientation.put(Configuration.ORIENTATION_SQUARE, "ORIENTATION_SQUARE");
        _orientation.put(Configuration.ORIENTATION_UNDEFINED, "ORIENTATION_UNDEFINED");

        _nav.put(Configuration.NAVIGATION_DPAD, "NAVIGATION_DPAD");
        _nav.put(Configuration.NAVIGATION_NONAV, "NAVIGATION_NONAV");
        _nav.put(Configuration.NAVIGATION_TRACKBALL, "NAVIGATION_TRACKBALL");
        _nav.put(Configuration.NAVIGATION_UNDEFINED, "NAVIGATION_UNDEFINED");
        _nav.put(Configuration.NAVIGATION_WHEEL, "NAVIGATION_WHEEL");

        _navHidden.put(Configuration.NAVIGATIONHIDDEN_NO, "NAVIGATIONHIDDEN_NO");
        _navHidden.put(Configuration.NAVIGATIONHIDDEN_YES, "NAVIGATIONHIDDEN_YES");
        _navHidden.put(Configuration.NAVIGATIONHIDDEN_UNDEFINED, "NAVIGATIONHIDDEN_UNDEFINED");

        _touchScreen.put(Configuration.TOUCHSCREEN_FINGER, "TOUCHSCREEN_FINGER");
        _touchScreen.put(Configuration.TOUCHSCREEN_NOTOUCH, "TOUCHSCREEN_NOTOUCH");
        _touchScreen.put(Configuration.TOUCHSCREEN_STYLUS, "TOUCHSCREEN_STYLUS");
        _touchScreen.put(Configuration.TOUCHSCREEN_UNDEFINED, "TOUCHSCREEN_UNDEFINED");

        _screenSize.put(Configuration.SCREENLAYOUT_SIZE_LARGE, "SCREENLAYOUT_SIZE_LARGE");
        _screenSize.put(Configuration.SCREENLAYOUT_SIZE_NORMAL, "SCREENLAYOUT_SIZE_NORMAL");
        _screenSize.put(Configuration.SCREENLAYOUT_SIZE_SMALL, "SCREENLAYOUT_SIZE_SMALL");
        _screenSize.put(Configuration.SCREENLAYOUT_SIZE_UNDEFINED, "SCREENLAYOUT_SIZE_UNDEFINED");
        _screenSize.put(Configuration.SCREENLAYOUT_SIZE_XLARGE, "SCREENLAYOUT_SIZE_XLARGE");

        _screenLong.put(Configuration.SCREENLAYOUT_LONG_NO, "SCREENLAYOUT_LONG_NO");
        _screenLong.put(Configuration.SCREENLAYOUT_LONG_YES, "SCREENLAYOUT_LONG_YES");
        _screenLong.put(Configuration.SCREENLAYOUT_LONG_UNDEFINED, "SCREENLAYOUT_LONG_UNDEFINED");

        _screenLayoutDir.put(Configuration.SCREENLAYOUT_LAYOUTDIR_LTR, "SCREENLAYOUT_LAYOUTDIR_LTR");
        _screenLayoutDir.put(Configuration.SCREENLAYOUT_LAYOUTDIR_RTL, "SCREENLAYOUT_LAYOUTDIR_RTL");
        _screenLayoutDir.put(Configuration.SCREENLAYOUT_LAYOUTDIR_SHIFT, "SCREENLAYOUT_LAYOUTDIR_SHIFT");
        _screenLayoutDir.put(Configuration.SCREENLAYOUT_LAYOUTDIR_UNDEFINED, "SCREENLAYOUT_LAYOUTDIR_UNDEFINED");

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctnr = (LinearLayout) findViewById(R.id.ctnr);
        final Point p = new Point();
        final Rect dispRect = new Rect();
        final float density = DispUtil.getDensity(this);// init density
        addRow(ctnr, "Display", "", true);
        DispUtil.getDisplay(this).getSize(p);
        addRow(ctnr, "Disp size", p.x + " x " + p.y);
        if (Build.VERSION.SDK_INT >= 17) {
            DispUtil.getDisplay(this).getRealSize(p);
            addRow(ctnr, "Disp real size", p.x + " x " + p.y);
        }
        DispUtil.getDisplay(this).getRectSize(dispRect);
        addRow(ctnr, "Disp rect size", dispRect.width() + " x " + dispRect.height());
        addRow(ctnr, "Disp rect border", "t:" + dispRect.top + " l:" + dispRect.left + " b:" + dispRect.bottom + " r:" + dispRect.right);
        addRow(ctnr, "Density", density);
        addRow(ctnr, "Rotation", DispUtil.getDisplay(this).getRotation());
        addRow(ctnr, "Disp ID", DispUtil.getDisplay(this).getDisplayId());
        addRow(ctnr, "Refresh rate", DispUtil.getDisplay(this).getRefreshRate());

        addRow(ctnr, "Metrics", "", true);
        final DisplayMetrics dm = DispUtil.getMetrics(MainActivity.this);
        addMetrics(dm);

        addRow(ctnr, "Real Metrics", "", true);
        final DisplayMetrics realdm;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            realdm = DispUtil.getRealMetrics(MainActivity.this);
            addMetrics(realdm);

            addRow(ctnr, "Calculated", "", true);
            addRow(ctnr, "Screen Real size dp", realdm.widthPixels / realdm.density + " x " + realdm.heightPixels / realdm.density);
            float xInches = realdm.widthPixels / dm.xdpi;
            float yInches = realdm.heightPixels / dm.ydpi;
            addRow(ctnr, "Screen Real size inches", xInches + " x " + yInches);
            addRow(ctnr, "Screen Real size mm", xInches * MM_IN_INCH + " x " + yInches * MM_IN_INCH);
            addRow(ctnr, "Screen size inches", "" + Math.sqrt(Math.pow(xInches, 2) + Math.pow(yInches, 2)));
            addRow(ctnr, "Aspect", realdm.widthPixels / (float) realdm.heightPixels);
        }
        addRow(ctnr, "Resources", "", true);
        addRow(ctnr, "DPI class", getResources().getString(R.string.resdpi));

        addRow(ctnr, "Build Data", "", true);
        addRow(ctnr, "brand/model", Build.BRAND + "/" + Build.MODEL);
        addRow(ctnr, "Product", Build.PRODUCT);
        addRow(ctnr, "SDK", Build.VERSION.SDK);
        addRow(ctnr, "sdk int", Build.VERSION.SDK_INT);
        addRow(ctnr, "inc", Build.VERSION.INCREMENTAL);
        addRow(ctnr, "rel", Build.VERSION.RELEASE);
        addRow(ctnr, "display", Build.DISPLAY);
        addRow(ctnr, "hardware", Build.HARDWARE);
        addRow(ctnr, "bootloader", Build.BOOTLOADER);
        addRow(ctnr, "Build device", Build.DEVICE);
        addRow(ctnr, "Build host", Build.HOST);
        addRow(ctnr, "Build ID", Build.ID);
        addRow(ctnr, "Tags", Build.TAGS);
        addRow(ctnr, "type", Build.TYPE);

        final Configuration configuration = getResources().getConfiguration();
        addRow(ctnr, "Configuration", "", true);
        addRow(ctnr, "orientation", getMapVal(configuration.orientation, _orientation));
        if (Build.VERSION.SDK_INT >= 17) {
            addRow(ctnr, "densityDpi", configuration.densityDpi);
        }
        addRow(ctnr, "screenHeightDp", configuration.screenHeightDp);
        addRow(ctnr, "screenWidthDp", configuration.screenWidthDp);
        addRow(ctnr, "smallestScreenWidthDp", configuration.smallestScreenWidthDp);
        final int sz = configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        final int longScreen = configuration.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK;
        final int layoutDir = configuration.screenLayout & Configuration.SCREENLAYOUT_LAYOUTDIR_MASK;

        addRow(ctnr, "screenLayout-size", getMapVal(sz, _screenSize));
        addRow(ctnr, "screenLayout-long", getMapVal(longScreen, _screenLong));
        addRow(ctnr, "screenLayout-layoutDir", getMapVal(layoutDir, _screenLayoutDir));
        addRow(ctnr, "fontScale", configuration.fontScale);
        addRow(ctnr, "hardKeyboardHidden", configuration.hardKeyboardHidden);
        addRow(ctnr, "keyboard", configuration.keyboard);
        addRow(ctnr, "mcc", configuration.mcc);
        addRow(ctnr, "mnc", configuration.mnc);
        addRow(ctnr, "navigation", getMapVal(configuration.navigation, _nav));
        addRow(ctnr, "navigationHidden", getMapVal(configuration.navigationHidden, _navHidden));
        addRow(ctnr, "touchscreen", getMapVal(configuration.touchscreen, _touchScreen));
        addRow(ctnr, "uiMode", configuration.uiMode);

        addRow(ctnr, "Time", "", true);
        addRow(ctnr, "currentTimeMillis", System.currentTimeMillis());
        addRow(ctnr, "currentTimeMillis", new Date(System.currentTimeMillis()).toString());
        addRow(ctnr, "uptimeMillis", SystemClock.uptimeMillis());
        addRow(ctnr, "uptimeMillis", new Date(SystemClock.uptimeMillis()).toString());

        addRow(ctnr, "Storage", "", true);
        addRow(ctnr, "mounted", DiskUtil.sdMounted());
        Boolean value = DiskUtil.sdWriteState();
        addRow(ctnr, "writeState", value != null ? Boolean.toString(value) : "None");
        addRow(ctnr, "dir", Environment.getExternalStorageDirectory().getAbsolutePath());
        addRow(ctnr, "state", Environment.getExternalStorageState());
        addRow(ctnr, "poddir", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).getAbsolutePath());

        addRow(ctnr, "Volumes", " -------", true);
        StorageOptions.determineStorageOptions(MainActivity.this);
        for (final StorageOptions.Volume v : StorageOptions._volumes) {
            addRow(ctnr, "Volume", "", true);
            addRow(ctnr, "path", v.getReadPath() != null ? v.getReadPath().getAbsolutePath() : null);
            addRow(ctnr, "free", v.freeMB);
            addRow(ctnr, "available", v.availableMB);
            addRow(ctnr, "total", v.availableMB);
            addRow(ctnr, "label", v.label);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            addRow(ctnr, "4.4 Volumes", " -------", true);
            final File[] files = getExternalFilesDirs(null);
            for (final File f : files) {
                if (f != null) {
                    addRow(ctnr, "Volume", "", true);
                    addRow(ctnr, "path", f.getAbsolutePath());
                    addRow(ctnr, "writeable", f.canWrite());
                    addRow(ctnr, "free", f.getUsableSpace() / 1024 / 1024);
                    addRow(ctnr, "total", f.getTotalSpace() / 1024 / 1024);
                }
            }
        }
        checkTelephony();
    }

    private void checkTelephony() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                addTelephony();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PERM_REQUEST_CODE);
            }
        } else {
            addTelephony();
        }
    }

    @SuppressLint("MissingPermission")
    private void addTelephony() {
        TelephonyManager systemService = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        addRow(ctnr, "Telephony", " -------", true);
        addRow(ctnr, "IMEI", systemService.getDeviceId());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERM_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            addTelephony();
        }
    }

    private void addMetrics(DisplayMetrics dm) {
        addRow(ctnr, "density", dm.density);
        addRow(ctnr, "densityDpi", dm.densityDpi);
        addRow(ctnr, "scaledDensity", dm.scaledDensity);
        addRow(ctnr, "widthPixels", dm.widthPixels);
        addRow(ctnr, "heightPixels", dm.heightPixels);
        addRow(ctnr, "xdpi", dm.xdpi);
        addRow(ctnr, "ydpi", dm.ydpi);
    }

    public String getMapVal(final int val, final SparseArray<String> map) {
        return "[" + val + "] " + map.get(val);
    }

    private void addRow(final LinearLayout ctnr, final String name, final long value) {
        addRow(ctnr, name, Long.toString(value));
    }

    private void addRow(final LinearLayout ctnr, final String name, final int value) {
        addRow(ctnr, name, Integer.toString(value));
    }

    private void addRow(final LinearLayout ctnr, final String name, final boolean value) {
        addRow(ctnr, name, Boolean.toString(value));
    }

    private void addRow(final LinearLayout ctnr, final String name, final float value) {
        addRow(ctnr, name, Float.toString(value));
    }

    private void addRow(final LinearLayout ctnr, final String name, final String value) {
        addRow(ctnr, name, value, false);
    }

    private void addRow(final LinearLayout ctnr, final String name, final String value, final boolean header) {
        final FrameLayout fl = new FrameLayout(this);
        LinearLayout.inflate(this, R.layout.fragment_row, fl);
        final TextView n = fl.findViewById(R.id.name);
        n.setText(name);
        if (header) {
            n.setTextSize(22);
            n.setPadding(DispUtil.dp(5), DispUtil.dp(3), DispUtil.dp(3), 0);
            n.setTag(Boolean.TRUE);
            n.setTextColor(Color.WHITE);
            fl.setBackgroundColor(Color.DKGRAY);
        }
        final TextView v = fl.findViewById(R.id.value);
        v.setText(value);
        ctnr.addView(fl);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem findItem = menu.findItem(R.id.action_send);
        findItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(final MenuItem item) {

                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < ctnr.getChildCount(); i++) {

                    final View vw = ctnr.getChildAt(i);
                    if (vw instanceof FrameLayout) {
                        final FrameLayout fl = (FrameLayout) vw;
                        final TextView n = fl.findViewById(R.id.name);
                        final TextView v = fl.findViewById(R.id.value);
                        v.setTextIsSelectable(true);
                        v.setSelectAllOnFocus(true);
                        if (n.getTag() != null && (Boolean) n.getTag()) {
                            sb.append("\n").append(n.getText()).append("\n").append("--------------------------------").append("\n");
                        } else {
                            sb.append(n.getText()).append(" : ").append(v.getText()).append("\n");
                        }
                    }
                }
                SendObjectParent.launchSend(MainActivity.this, "Android Device Data : " + Build.BRAND + "/" + Build.MODEL, sb.toString());
                return true;
            }
        });
        return true;
    }

}
