package codes.nh.webvideobrowser.screens.browser;

import android.graphics.Bitmap;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class BrowserChromeClient extends WebChromeClient {

    @Override
    public void onReceivedTitle(WebView webView, String title) {
        super.onReceivedTitle(webView, title);

        Browser browser = (Browser) webView;
        browser.getListener().onUpdateTitle(title);
    }

    @Override
    public void onReceivedIcon(WebView webView, Bitmap favicon) {
        super.onReceivedIcon(webView, favicon);

        Browser browser = (Browser) webView;
        browser.getListener().onUpdateFavicon(favicon);
    }

    @Override
    public void onProgressChanged(WebView webView, int progress) {
        super.onProgressChanged(webView, progress);

        Browser browser = (Browser) webView;
        browser.getListener().onUpdateProgress(progress);
    }

}