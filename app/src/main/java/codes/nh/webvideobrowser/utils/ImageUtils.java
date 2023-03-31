package codes.nh.webvideobrowser.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import androidx.collection.LruCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImageUtils {

    private static LruCache<String, Bitmap> BITMAP_CACHE;

    private static List<String> NOT_FOUND_LIST;

    public static void initializeBitmapCache() {
        if (BITMAP_CACHE != null) {
            return;
        }

        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        BITMAP_CACHE = new LruCache<>(cacheSize);

        NOT_FOUND_LIST = new ArrayList<>();
    }

    public static void setImageViewFromUrl(ImageView imageView, String url, int loadingIcon, int compress) {
        imageView.setImageResource(loadingIcon);

        if (url == null || NOT_FOUND_LIST.contains(url)) {
            return;
        }

        Bitmap bitmap = BITMAP_CACHE.get(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }

        Async.execute(new Async.ResultTask<Bitmap>() {
            @Override
            public Bitmap doAsync() {
                return getBitmapFromUrl(url, compress);
            }

            @Override
            public void doSync(Bitmap bitmap) {
                if (bitmap == null) {
                    NOT_FOUND_LIST.add(url);
                    return;
                }

                imageView.setImageBitmap(bitmap);
                BITMAP_CACHE.put(url, bitmap);
            }
        }, 3000L);
    }

    private static Bitmap getBitmapFromUrl(String url, int compress) {
        try (InputStream stream = new URL(url).openStream();) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = compress;
            return BitmapFactory.decodeStream(stream, null, options);
        } catch (IOException e) {
            AppUtils.log("getBitmapFromUrl(" + url + ")", e);
        }
        return null;
    }
}
