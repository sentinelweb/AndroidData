package co.uk.sentinelweb.androidgetdata.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public class SendObjectParent {

    private static String ver = null;


    public static void launchSend(Activity act, String subject, String content) {
        launchSend(act, subject, content, null);
    }

    public static boolean launchSend(Activity act, String subject, String content, String to) {
        try {
            String[] addressList = null;
            String toURL = "mailto://";
            if (to != null) {
                toURL += to;
                addressList = new String[]{to};
            }
            Intent myIntent = new Intent(Intent.ACTION_SEND, Uri.parse(toURL));
            myIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            if (addressList != null) {
                myIntent.putExtra(Intent.EXTRA_EMAIL, addressList);
            }
            myIntent.putExtra(Intent.EXTRA_TEXT, content + getPhoneDetails(act));
            myIntent.setType("message/rfc822");
            act.startActivity(myIntent);
            return true;
        } catch (Exception e) {
            Toast.makeText(act, "No mail action configured : you may need to install one", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    static String getPhoneDetails(Context c) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nPhone ");
        sb.append("brand/model:");
        sb.append(Build.BRAND);
        sb.append("/");
        sb.append(Build.MODEL);
        sb.append("\n ");
        sb.append("Version:");
        sb.append(getVersion(c));
        sb.append(" inc:");
        sb.append(Build.VERSION.INCREMENTAL);
        sb.append(" sdk:");
        sb.append(Build.VERSION.SDK);
        sb.append(" rel:");
        sb.append(Build.VERSION.RELEASE);
        sb.append(" disp:");
        getDisplaySize(c, sb);
        return sb.toString();
    }

    static void getDisplaySize(Context c, StringBuilder sb) {
        DisplayMetrics metrics = new DisplayMetrics();
        getDisplay(c).getMetrics(metrics);
        sb.append(metrics.widthPixels).append("x").append(metrics.heightPixels).append(" (density:").append(metrics.density).append(")");
        //return metrics.widthPixels+"x"+metrics.heightPixels+":"+metrics.density;
    }


    static @NonNull
    Display getDisplay(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay();
    }

    public static void launchMarket(Activity act, String packagename) {
        try {
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(packagename));
            act.startActivity(myIntent);
        } catch (Throwable e) {
            Toast.makeText(act, "Couldn't load market application.", Toast.LENGTH_LONG).show();
        }
    }

    static String getVersion(Context c) {
        if (ver == null) {
            try {
                ver = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
            } catch (NameNotFoundException e) {
                ver = "Unknown";
            }
        }
        return ver;
    }

    public static void share(final Context c, final String body, final String imagePath, final String type) {
        MediaScannerConnectionClient mediaScannerClient = new MediaScannerConnectionClient() {
            private MediaScannerConnection msc = null;

            {
                msc = new MediaScannerConnection(c, this);
                msc.connect();
            }

            public void onMediaScannerConnected() {
                msc.scanFile(imagePath, null);
            }

            public void onScanCompleted(String path, Uri uri) {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra("sms_body", body);
                sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                sendIntent.setType(type);
                c.startActivity(sendIntent);
                msc.disconnect();
            }
        };
    }

    public static void shareURL(Context c, String body, Uri remoteUrl, String type) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra("sms_body", body);
        sendIntent.putExtra(Intent.EXTRA_STREAM, remoteUrl);
        sendIntent.setType(type);
        c.startActivity(sendIntent);
    }

    public static void shareURL(Context c, String body, String subject, Uri remoteUrl) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra("sms_body", body);
        i.putExtra(Intent.EXTRA_TEXT, body);
        c.startActivity(Intent.createChooser(i, "Share URL"));
        //c.startActivity(sendIntent);
    }

    public static void share(Context c, String body) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra("sms_body", body);
        //sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setType("image/png");
        c.startActivity(sendIntent);
        // msc.disconnect();
    }
}
