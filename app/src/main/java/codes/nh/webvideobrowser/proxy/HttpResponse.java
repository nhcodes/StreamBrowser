package codes.nh.webvideobrowser.proxy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

public class HttpResponse {

    int statusCode;

    InputStream content;

    Map<String, String> headers;

    Integer length = null;

    public HttpResponse(int statusCode, InputStream content, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.content = content;
        this.headers = headers;
    }

    public HttpResponse(int statusCode, byte[] content, Map<String, String> headers) {
        this(statusCode, new ByteArrayInputStream(content), headers);
        length = content.length;
    }

    public HttpResponse(int statusCode, String content, Map<String, String> headers) {
        this(statusCode, content.getBytes(), headers);
        length = content.length();
    }


}
