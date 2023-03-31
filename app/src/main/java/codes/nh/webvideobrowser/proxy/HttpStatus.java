package codes.nh.webvideobrowser.proxy;

public enum HttpStatus {

    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request");

    final int code;

    final String message;

    HttpStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getResponseLine() {
        return "HTTP/1.1 " + code + " " + message;
    }

    public static HttpStatus fromCode(int code) {
        for (HttpStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}