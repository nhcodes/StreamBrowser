package codes.nh.webvideobrowser.proxy;

public enum HttpStatus {

    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    INTERVAL_SERVER_ERROR(500, "Internal Server Error");

    final int code;

    final String message;

    HttpStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getResponseLine() {
        return "HTTP/1.1 " + code + " " + message;
    }

}