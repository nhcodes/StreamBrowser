package codes.nh.streambrowser.proxy;

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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codes.nh.streambrowser.utils.AppUtils;
import codes.nh.streambrowser.utils.UrlUtils;

public class ProxyServer extends HttpServer {

    private final Context context;

    private final String ip;

    public ProxyServer(Context context, int port) {
        super(port);
        this.context = context;
        this.ip = loadLocalIp();
    }

    private String loadLocalIp() {
        List<String> ips = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress address = inetAddresses.nextElement();
                    if (address.isSiteLocalAddress()) {
                        ips.add(address.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            AppUtils.log("NetworkInterface.getNetworkInterfaces", e);
        }

        AppUtils.log("local ips: " + String.join(", ", ips));

        //https://www.rfc-editor.org/rfc/rfc1918
        //for some reason 10. ips don't work, haven't tested 172.
        String localIp = ips.stream().filter(ip -> ip.startsWith("192.168.")).findAny().orElse(null);
        if (localIp == null) { //todo
            AppUtils.log("localIp == null: using localhost address (won't work)");
            return "127.0.0.1";
        }
        return localIp;
    }

    public String getHttpAddress() {
        return "http://" + ip + ":" + getPort() + "/";
    }

    @Override
    public HttpResponse handleRequest(String path, int id) throws Exception {

        if (path.equals("")) {
            return new HttpResponse(HttpStatus.OK, "proxy server is running", new HashMap<>());
        }

        String fileName = UrlUtils.getFileNameFromUrl(getHttpAddress() + path);
        String query = path.replace(fileName, "");

        Pair<String, Map<String, String>> decodedQuery = getDecodedQuery(query);
        if (decodedQuery == null) {
            AppUtils.log(id + " query decode error");
            return null;//return new HttpResponse(HttpStatus.BAD_REQUEST, "query decode error", new HashMap<>());
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

                /*int responseCode = connection.getResponseCode();
                if (String.valueOf(responseCode).charAt(0) != '2') {
                    AppUtils.log(id + " remote connect error: " + responseCode);
                    return null;//new HttpResponse(HttpStatus.BAD_REQUEST, "remote connect error", new HashMap<>());
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

    public String convertToProxyUrl(String url, Map<String, String> headers) {
        return getHttpAddress() + UrlUtils.getFileNameFromUrl(url) + getEncodedQuery(url, headers);
    }

    private String convertHlsPlayList(InputStream inputStream, Map<String, String> requestHeaders, String host) throws Exception {
        try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);) {

            StringBuilder stringBuilder = new StringBuilder();

            String line = bufferedReader.readLine();
            if (line == null || !line.equals("#EXTM3U")) {
                throw new Exception("hls playlist not valid");
            }
            stringBuilder.append(line).append("\r\n");

            while ((line = bufferedReader.readLine()) != null) {
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

    private String getEncodedQuery(String url, Map<String, String> headers) {
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

    private Pair<String, Map<String, String>> getDecodedQuery(String query) {
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
