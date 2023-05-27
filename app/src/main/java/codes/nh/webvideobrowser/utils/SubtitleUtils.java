package codes.nh.webvideobrowser.utils;

import androidx.media3.common.MimeTypes;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

public class SubtitleUtils {

    public static Map<String, String> SUBTITLE_MIME_TYPES = Map.of(
            "vtt", MimeTypes.TEXT_VTT,
            "srt", MimeTypes.APPLICATION_SUBRIP,
            "ttml", MimeTypes.APPLICATION_TTML,
            "ssa", MimeTypes.TEXT_SSA,
            "ass", MimeTypes.TEXT_SSA
    );

    //https://developer.android.com/guide/topics/media/exoplayer/supported-formats#standalone_subtitle_formats
    public static String getSubtitleMimeTypeFromExtension(String extension) {
        return SUBTITLE_MIME_TYPES.get(extension);
    }

    public static JSONArray requestSubtitles(String query, String language) {
        try {
            query = query.replaceAll("\\(.+\\)", "");
            query = query.replaceAll("[^A-Za-z0-9\\s]", "");
            query = URLEncoder.encode(query, "UTF-8");

            String apiUrl = "https://rest.opensubtitles.org/search"
                    + "/query-" + query
                    + "/sublanguageid-" + language;

            AppUtils.log("subs requested: " + apiUrl);

            URL url = new URL(apiUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "TemporaryUserAgent");

            if (connection.getResponseCode() != 200) {
                return null;
            }

            InputStream inputStream = connection.getInputStream();

            String result = readStreamToString(inputStream);
            JSONArray array = new JSONArray(result);

            inputStream.close();

            return array;

        } catch (Exception e) {
            AppUtils.log("requestSubtitles", e);
        }
        return null;
    }

    public static String downloadSubtitle(String downloadUrl) {
        try {

            URL url = new URL(downloadUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            InputStream inputStream = connection.getInputStream();
            GZIPInputStream gzInputStream = new GZIPInputStream(inputStream);

            String result = readStreamToString(gzInputStream);

            gzInputStream.close();
            inputStream.close();

            return result;

        } catch (Exception e) {
            AppUtils.log("downloadSubtitle", e);
        }
        return null;
    }

    private static String readStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }

        String result = outputStream.toString("UTF-8");

        outputStream.close();

        return result;
    }

    public static String convertSrtToVtt(String subtitles) {
        return "WEBVTT\n\n" + subtitles.replaceAll("(\\d{2}),(\\d{3})", "$1.$2");
    }

    /*
    private void getSubtitles(Stream stream) {
        new AsyncExecution().execute(new AsyncExecution.Request() {
            @Override
            public Object onRequest() {
                return SubtitleUtils.requestSubtitles(stream.getTitle(), "eng");
            }

            @Override
            public void onResult(Object result) {
                JSONArray array = (JSONArray) result;
                AppUtils.log( "subs result: " + array);
                if (array == null) {
                    getHistory(stream);
                    return;
                }

                int amount = 5;
                if (array.length() < amount) {
                    amount = array.length();
                }

                try {
                    for (int i = 0; i < amount; i++) {
                        JSONObject object = array.getJSONObject(i);
                        String downloadLink = object.getString("SubDownloadLink");
                        stream.getSubtitleUrls().add(downloadLink);
                    }
                } catch (Exception e) {
                    AppUtils.log( "getSubtitles: " + e.getMessage());
                }

                getHistory(stream);
            }
        });
    }*/

    /*
    new AsyncExecution().execute(new AsyncExecution.Request() {
        @Override
        public Object onRequest() {
            JSONArray array = SubtitleUtils.requestSubtitles(stream.getTitle(), "eng");
            String subs = "";
            try {
                subs = SubtitleUtils.downloadSubtitle(array.getJSONObject(0).getString("SubDownloadLink"));
            } catch (Exception e) {
                AppUtils.log( "Error subssss: " + e.getMessage());
            }
            return subs;
        }

        @Override
        public void onResult(Object result) {
            String subs = (String) result;
            subs = SubtitleUtils.convertSrtToVtt(subs);
            sendMessage(subs);
        }
    });*/
}
