package codes.nh.webvideobrowser.fragments.browser;

import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import codes.nh.webvideobrowser.fragments.stream.Stream;
import codes.nh.webvideobrowser.utils.UrlUtils;

public class BrowserClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
        Browser browser = (Browser) webView;

        String oldUrl = webView.getUrl();
        String oldDomain = UrlUtils.getDomainNameFromURL(oldUrl);

        String newUrl = request.getUrl().toString();
        String newDomain = UrlUtils.getDomainNameFromURL(newUrl);

        //true = block request, false = allow request;
        boolean blockRequest = false;
        if (!newDomain.equalsIgnoreCase(oldDomain)) {
            blockRequest = !browser.getListener().onRedirect(oldUrl, newUrl);
        }

        if (!blockRequest) {
            browser.getListener().onRequestPage(newUrl);
        }

        return blockRequest;
    }

    //private final List<String> cachedThumbnails = new ArrayList<>();

    private final List<String> cachedSubtitles = new ArrayList<>();

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest request) {

        String url = request.getUrl().toString();
        String fileName = UrlUtils.getFileNameFromUrl(url);

        Map<String, String> headers = request.getRequestHeaders();

        /*if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) { //thumbnails

            synchronized (cachedThumbnails) {
                cachedThumbnails.add(url);
            }

        } else */
        if (fileName.endsWith(".vtt") || fileName.endsWith(".srt")) { //subtitles

            synchronized (cachedSubtitles) {
                cachedSubtitles.add(url);
            }

        } else if (fileName.endsWith(".m3u8") || fileName.endsWith(".mp4")) { //streams

            onFindStream(webView, url, headers);

        }/* else if (fileName.endsWith("videoplayback")) { // youtube
            url = url.replaceAll("&range=\\d*-\\d*", "&");
            onFindStream(webView, url, headers);
        } else {*/

        //onFindStream(webView, url, headers);
            /* else { //streams 2

            String range = UrlUtils.getHeaderValue(headers, "range");
            if (range != null && range.startsWith("bytes=")) {
                onFindStream(webView, url, headers);
            }

        }
        }*/

        return super.shouldInterceptRequest(webView, request);
    }

    private void onFindStream(WebView webView, String url, Map<String, String> headers) {
        webView.post(() -> {

            Stream stream = new Stream(
                    url,
                    webView.getUrl(),
                    webView.getTitle(),
                    headers,
                    new ArrayList<>(/*cachedThumbnails*/),
                    new ArrayList<>(cachedSubtitles),
                    0
            );

            Browser browser = (Browser) webView;
            browser.getListener().onFindStream(stream);

        });
    }

    @Override
    public void onPageStarted(WebView webView, String url, Bitmap favicon) {
        super.onPageStarted(webView, url, favicon);

        //cachedThumbnails.clear();
        cachedSubtitles.clear();

        Browser browser = (Browser) webView;
        browser.getListener().onStartLoadPage(url);
    }

    @Override
    public void onPageFinished(WebView webView, String url) {
        super.onPageFinished(webView, url);

        Browser browser = (Browser) webView;
        browser.getListener().onFinishLoadPage(url);
    }

    @Override
    public void doUpdateVisitedHistory(WebView webView, String url, boolean isReload) {
        super.doUpdateVisitedHistory(webView, url, isReload);

        Browser browser = (Browser) webView;
        browser.getListener().onUpdateUrl(url);
    }

    /*
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        AppUtils.log("onReceivedError: " + error.getErrorCode() + " - " + error.getErrorCode());
    }

    @Override //todo get webresourceresponse for non error requests
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        AppUtils.log("onReceivedHttpError: " + errorResponse.getStatusCode() + " - " + errorResponse.getReasonPhrase() + " - " + request.getUrl() + " - " + AppUtils.mapToJson(errorResponse.getResponseHeaders()).toString());
    }*/
}