package codes.nh.webvideobrowser.proxy;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Pair;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.UrlUtils;

public class ProxyServer extends HttpServer {

    private final Context context;

    public ProxyServer(Context context, int port) {
        super(port);
        this.context = context;
    }

    public void startProxy() {
        ProxyService.start(context);
        start();
    }

    public void stopProxy() {
        stop();
        ProxyService.stop(context);
    }

    @Override
    public HttpResponse handleRequest(String path, int id) throws Exception {

        if (path.equals("")) {
            return new HttpResponse(HttpStatus.OK, "proxy server is running", new HashMap<>());
        }

        String fileName = UrlUtils.getFileNameFromUrl(getIpAddress() + path);
        String query = path.replace(fileName, "");

        Pair<String, Map<String, String>> decodedQuery = getDecodedQuery(query);
        if (decodedQuery == null) {
            AppUtils.log(id + " query decode error");
            return null;//new HttpResponse(400, "query decode error".getBytes(), new HashMap<>());
        }

        long startTime = System.currentTimeMillis();

        String remoteUrl = decodedQuery.first;
        Map<String, String> remoteRequestHeaders = decodedQuery.second;
        AppUtils.log(id + " remote url is " + remoteUrl);

        Map<String, String> responseHeaders = new HashMap<>();
        InputStream responseContent;

        if (remoteUrl.startsWith("content://")) { //local file

            responseContent = context.getContentResolver().openInputStream(Uri.parse(remoteUrl));

            AppUtils.log(id + " file read took " + (System.currentTimeMillis() - startTime));

        } else { //remote url

            HttpURLConnection connection = UrlUtils.connectToUrl(remoteUrl, remoteRequestHeaders);

            if (fileName.contains(".m3u8")) {

                /*int responseCode = remoteResponse.statusCode;
                if (String.valueOf(responseCode).charAt(0) != '2') {
                    AppUtils.log(id + " remote connect error: " + responseCode);
                    return null;//new HttpResponse(400, "remote connect error".getBytes(), new HashMap<>());
                }*/

                String hostName = UrlUtils.getAddressWithoutFileName(remoteUrl);
                String playlist = convertHlsPlayList(connection.getInputStream(), remoteRequestHeaders, hostName);
                responseContent = new ByteArrayInputStream(playlist.getBytes());

            } else {

                responseContent = connection.getInputStream();

            }

            AppUtils.log(id + " remote read took " + (System.currentTimeMillis() - startTime));

        }

        return new HttpResponse(HttpStatus.OK, responseContent, responseHeaders);

    }

    //convert

    private String convertHlsPlayList(InputStream is, Map<String, String> requestHeaders, String host) throws Exception {
        try (InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr);) {

            StringBuilder stringBuilder = new StringBuilder();

            String line = br.readLine();
            if (line == null || !line.equals("#EXTM3U")) {
                throw new Exception("hls playlist not valid");
            }
            stringBuilder.append(line).append("\r\n");

            while ((line = br.readLine()) != null) {
                if (line.length() != 0 && !line.startsWith("#")) {
                    String remoteUrl;
                    if (line.startsWith("http")) {
                        remoteUrl = line;
                    } else {
                        remoteUrl = host + line;
                    }
                    //AppUtils.log(remoteUrl);
                    String query = getEncodedQuery(remoteUrl, requestHeaders);
                    String fileName = UrlUtils.getFileNameFromUrl(remoteUrl);
                    String proxyHost = fileName + query;
                    stringBuilder.append(proxyHost).append("\r\n");
                } else {
                    stringBuilder.append(line).append("\r\n");
                }
            }

            return stringBuilder.toString();
        }
    }

    //encode / decode

    public String getEncodedQuery(String url, Map<String, String> headers) {
        try {
            JSONObject castStreamInfoJson = new JSONObject();
            castStreamInfoJson.put("u", url);
            castStreamInfoJson.put("h", AppUtils.mapToJson(headers));
            String queryEncoded = encodeBase64(castStreamInfoJson.toString());
            return "?q=" + queryEncoded;
        } catch (Exception e) {
            AppUtils.log("getEncodedQuery()", e);
        }
        return null;
    }

    public Pair<String, Map<String, String>> getDecodedQuery(String query) {
        try {
            String queryDecoded = decodeBase64(query.replace("?q=", ""));
            JSONObject castStreamInfoJson = new JSONObject(queryDecoded);
            String url = castStreamInfoJson.getString("u");
            Map<String, String> headers = AppUtils.jsonToMap(castStreamInfoJson.getJSONObject("h"));
            return new Pair<>(url, headers);
        } catch (Exception e) {
            AppUtils.log("getDecodedQuery()", e);
        }
        return null;
    }

    private String encodeBase64(String in) {
        return new String(Base64.encode(in.getBytes(StandardCharsets.UTF_8), Base64.URL_SAFE | Base64.NO_WRAP));
    }

    private String decodeBase64(String in) {
        return new String(Base64.decode(in.getBytes(StandardCharsets.UTF_8), Base64.URL_SAFE | Base64.NO_WRAP));
    }

}
