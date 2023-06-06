package codes.nh.streambrowser.utils;

import android.net.Uri;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class UrlUtils {

    public static String getHostFromURL(String url) {
        String host = Uri.parse(url).getHost();
        if (host == null) {
            AppUtils.log("weird has no host " + url);
            //todo about:blank
        }
        return host != null ? host : "";
    }

    public static String getDomainNameFromURL(String url) {
        String host = getHostFromURL(url);
        String[] hostParts = host.split("\\.");
        int hostPartsLength = hostParts.length;
        if (hostPartsLength < 2) {
            return host;
        }
        return hostParts[hostPartsLength - 2] + "." + hostParts[hostPartsLength - 1];
    }

    public static String getFileNameFromUrl(String url) {
        String fileName = Uri.parse(url).getLastPathSegment();
        return fileName != null ? fileName : "";
    }

    public static String getAddressWithoutFileName(String url) {
        return url.substring(0, url.indexOf(getFileNameFromUrl(url)));
    }

    public static HttpURLConnection connectToUrl(String url, Map<String, String> headers) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        connection.connect();
        return connection;
    }

}
