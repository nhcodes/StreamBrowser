package codes.nh.webvideobrowser.utils;

import android.net.Uri;

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

    public static String getHeaderValue(Map<String, String> headers, String key) {
        return headers.entrySet()
                .stream()
                .filter(e -> e.getKey().equalsIgnoreCase(key))
                .map(e -> e.getValue())
                .findFirst()
                .orElse(null);
    }

    public static String getFaviconUrl(String url) {
        String hostName = UrlUtils.getHostFromURL(url);
        return "https://www.google.com/s2/favicons?sz=64&domain=" + hostName;
    }
}
