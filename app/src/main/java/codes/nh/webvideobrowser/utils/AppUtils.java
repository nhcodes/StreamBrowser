package codes.nh.webvideobrowser.utils;

import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;

import com.google.android.gms.cast.MediaError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import codes.nh.webvideobrowser.R;

public class AppUtils {

    //log

    private static final String LOG_TAG = "[WebVideoBrowser]";

    public static void log(String message) {
        Log.e(LOG_TAG, message);
    }

    public static void log(String message, Throwable error) {
        log("error " + message + ": \n" + Log.getStackTraceString(error));
    }

    //keyboard

    public static void closeKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name), text);
        clipboard.setPrimaryClip(clip);
    }

    //time

    public static String getTimeStringFromTimestamp(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(time);
    }

    public static String millisToMinutesSeconds(long millis) {
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    //cast errors

    public static String getMediaErrorName(int error) {
        for (Field field : MediaError.DetailedErrorCode.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                int value = field.getInt(null);
                if (value == error) {
                    return field.getName();
                }
            } catch (IllegalAccessException e) {
                AppUtils.log("getMediaErrorName(" + error + ")", e);
            }
        }
        return null;
    }

    //intents

    public static void openFile(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    public static void openShareDialog(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        context.startActivity(Intent.createChooser(intent, null));
    }

    //downloads

    public static void downloadFile(Context context, String url, Map<String, String> headers) {
        String fileName = UrlUtils.getFileNameFromUrl(url);
        DownloadManager.Request download = new DownloadManager.Request(Uri.parse(url));
        headers.forEach((k, v) -> download.addRequestHeader(k, v));
        download.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        download.allowScanningByMediaScanner();
        download.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(download);
    }

    //lists & maps

    public static JSONArray listToJson(List<String> list) {
        return new JSONArray(list);
    }

    public static List<String> jsonToList(JSONArray json) {
        List<String> list = new ArrayList<>();
        try {
            for (int i = 0; i < json.length(); i++) {
                list.add(json.getString(i));
            }
        } catch (JSONException e) {
            AppUtils.log("jsonToList()", e);
        }
        return list;
    }

    public static JSONObject mapToJson(Map<String, String> map) {
        return new JSONObject(map);
    }

    public static Map<String, String> jsonToMap(JSONObject json) {
        Map<String, String> map = new HashMap<>();
        try {
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                map.put(key, json.getString(key));
            }
        } catch (JSONException e) {
            AppUtils.log("jsonToMap()", e);
        }
        return map;
    }

    //streams

    private final static int BUFFER_SIZE = 8192;

    public static void copyTo(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer, 0, BUFFER_SIZE)) >= 0) {
            out.write(buffer, 0, read);
        }
    }

    //mediastore

    public static String getFileNameFromUri(Context context, Uri uri) {
        try (
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        ) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                return cursor.getString(columnIndex);
            }
        }
        return null;
    }

    //activity result

    public static <I, O> ActivityResultLauncher<I> registerActivityResultLauncher(
            ComponentActivity activity,
            ActivityResultContract<I, O> contract,
            ActivityResultCallback<O> callback
    ) {
        String key = UUID.randomUUID().toString();
        return activity.getActivityResultRegistry().register(key, contract, callback);
    }

}
