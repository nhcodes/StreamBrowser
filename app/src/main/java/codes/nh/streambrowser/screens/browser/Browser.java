package codes.nh.streambrowser.screens.browser;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import codes.nh.streambrowser.screens.stream.Stream;
import codes.nh.streambrowser.utils.AppUtils;

public class Browser extends WebView {

    public Browser(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initializeBrowser();
    }

    private void initializeBrowser() {
        WebSettings settings = getSettings();

        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        BrowserClient browserClient = new BrowserClient();
        setWebViewClient(browserClient);

        BrowserChromeClient browserChromeClient = new BrowserChromeClient();
        setWebChromeClient(browserChromeClient);

        //CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
    }

    private boolean desktopMode = false;

    public boolean getDesktopMode() {
        return desktopMode;
    }

    public void setDesktopMode(boolean enabled) {
        desktopMode = enabled;

        String userAgent = getSettings().getUserAgentString();
        if (enabled) {
            userAgent = userAgent
                    .replace(" Mobile ", " Dummy1 ")
                    .replace(" Android ", " Dummy2 ");
        } else {
            userAgent = userAgent
                    .replace(" Dummy1 ", " Mobile ")
                    .replace(" Dummy2 ", " Android ");
        }
        getSettings().setUserAgentString(userAgent);
        AppUtils.log("new user agent = " + userAgent);

        //getSettings().setUseWideViewPort(enabled);
    }

    @Override
    public void loadUrl(@NonNull String url) {
        super.loadUrl(url);
        getListener().onRequestPage(url);
    }

    @Override
    public void loadUrl(@NonNull String url, @NonNull Map<String, String> additionalHttpHeaders) {
        super.loadUrl(url, additionalHttpHeaders);
        getListener().onRequestPage(url);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getListener().onTouch();
        return super.onTouchEvent(event);
    }

    //listener

    private Listener listener;

    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    interface Listener {

        void onRequestPage(String url);

        void onStartLoadPage(String url);

        void onFinishLoadPage(String url);

        void onUpdateUrl(String url);

        void onUpdateTitle(String title);

        void onUpdateFavicon(Bitmap favicon);

        void onUpdateProgress(int progress);

        boolean onRedirect(String oldUrl, String newUrl);

        void onFindStream(Stream stream);

        void onTouch();
    }
}
