package codes.nh.webvideobrowser.proxy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

public class HttpResponse {

    final HttpStatus status;

    final InputStream content;

    final Map<String, String> headers;

    Integer length = null;

    public HttpResponse(HttpStatus status, InputStream content, Map<String, String> headers) {
        this.status = status;
        this.content = content;
        this.headers = headers;
    }

    public HttpResponse(HttpStatus status, byte[] content, Map<String, String> headers) {
        this(status, new ByteArrayInputStream(content), headers);
        length = content.length;
    }

    public HttpResponse(HttpStatus status, String content, Map<String, String> headers) {
        this(status, content.getBytes(), headers);
        length = content.length();
    }


}
