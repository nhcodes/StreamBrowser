package codes.nh.streambrowser.screens.browser;

import java.util.Collections;
import java.util.Map;

public class BrowserRequest {

    private final String url;

    private final Map<String, String> headers;

    public BrowserRequest(String url, Map<String, String> headers) {
        this.url = url;
        this.headers = headers;
    }

    public BrowserRequest(String url) {
        this(url, Collections.emptyMap());
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
