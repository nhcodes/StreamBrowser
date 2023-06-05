package codes.nh.webvideobrowser.screens.stream;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.common.images.WebImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.SubtitleUtils;
import codes.nh.webvideobrowser.utils.UrlUtils;

@Entity
public class Stream {

    @PrimaryKey
    @NonNull
    private final String streamUrl;

    private final String sourceUrl;

    private final String title;

    private final Map<String, String> headers;

    private final List<String> thumbnailUrls;

    private final List<String> subtitleUrls;

    private long startTime;

    private boolean useProxy = false;

    public Stream(@NonNull String streamUrl, String sourceUrl, String title, Map<String, String> headers, List<String> thumbnailUrls, List<String> subtitleUrls, long startTime) {
        this.streamUrl = streamUrl;
        this.sourceUrl = sourceUrl;
        this.title = title;
        this.headers = headers;
        this.thumbnailUrls = thumbnailUrls;
        this.subtitleUrls = subtitleUrls;
        this.startTime = startTime;
    }

    @NonNull
    public String getStreamUrl() {
        return streamUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getTitle() {
        return title;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public List<String> getThumbnailUrls() {
        return thumbnailUrls;
    }

    public List<String> getSubtitleUrls() {
        return subtitleUrls;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public boolean useProxy() {
        return useProxy;
    }

    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    public MediaLoadRequestData createMediaLoadRequestData(String contentUrl) {
        MediaMetadata metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        metadata.putString(MediaMetadata.KEY_TITLE, title);
        metadata.putString(MediaMetadata.KEY_SUBTITLE, sourceUrl.replaceFirst("https?://", ""));

        for (String thumbnailUrl : thumbnailUrls) {
            metadata.addImage(new WebImage(Uri.parse(thumbnailUrl)));
        }

        List<MediaTrack> subtitles = new ArrayList<>();
        int i = 0;
        for (String subtitleUrl : subtitleUrls) {
            MediaTrack mediaTrack = new MediaTrack.Builder(i, MediaTrack.TYPE_TEXT)
                    .setName(UrlUtils.getFileNameFromUrl(subtitleUrl))
                    .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                    .setContentId(subtitleUrl)
                    //.setLanguage("en-US")
                    .build();
            subtitles.add(mediaTrack);
            AppUtils.log("subtitleUrl=" + subtitleUrl);
            i++;
        }

        MediaInfo mediaInfo = new MediaInfo.Builder(contentUrl)
                .setMetadata(metadata)
                .setMediaTracks(subtitles)
                .setCustomData(toJson())
                .build();

        return new MediaLoadRequestData.Builder()
                .setMediaInfo(mediaInfo)
                .setCurrentTime(startTime)
                .build();
    }

    public MediaItem createMediaItem(String contentUrl) {
        androidx.media3.common.MediaMetadata metadata = new androidx.media3.common.MediaMetadata.Builder()
                .setTitle(title)
                .setSubtitle(sourceUrl.replaceFirst("https?://", ""))
                .build();

        List<MediaItem.SubtitleConfiguration> subtitles = new ArrayList<>();
        for (String subtitleUrl : subtitleUrls) {
            String[] fileNameParts = UrlUtils.getFileNameFromUrl(subtitleUrl).split("\\.", 2);
            String fileName = fileNameParts[0];
            String extension = fileNameParts.length == 2 ? fileNameParts[1] : "";
            String mime = SubtitleUtils.getSubtitleMimeTypeFromExtension(extension);
            MediaItem.SubtitleConfiguration subtitle = new MediaItem.SubtitleConfiguration
                    .Builder(Uri.parse(subtitleUrl))
                    .setLabel(fileName)
                    .setMimeType(mime)
                    .build();
            subtitles.add(subtitle);
        }

        return new MediaItem.Builder()
                .setUri(contentUrl)
                .setMediaMetadata(metadata)
                .setSubtitleConfigurations(subtitles)
                .build();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Stream) {
            Stream stream = (Stream) object;
            return streamUrl.equalsIgnoreCase(stream.getStreamUrl());
        }
        return false;
    }

    public JSONObject toJson() {
        return new JSONObject(Map.of(
                "streamUrl", streamUrl,
                "sourceUrl", sourceUrl,
                "title", title,
                "headers", AppUtils.mapToJson(headers),
                "thumbnailUrls", AppUtils.listToJson(thumbnailUrls),
                "subtitleUrls", AppUtils.listToJson(subtitleUrls),
                "startTime", startTime
        ));
    }

    public static Stream fromJson(JSONObject jsonObject) throws JSONException {
        return new Stream(
                jsonObject.getString("streamUrl"),
                jsonObject.getString("sourceUrl"),
                jsonObject.getString("title"),
                AppUtils.jsonToMap(jsonObject.getJSONObject("headers")),
                AppUtils.jsonToList(jsonObject.getJSONArray("thumbnailUrls")),
                AppUtils.jsonToList(jsonObject.getJSONArray("subtitleUrls")),
                jsonObject.getLong("startTime")
        );
    }

}
