package codes.nh.webvideobrowser.fragments.browser;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class BrowserDestination {

    @PrimaryKey
    @NonNull
    private final String url;

    private final String title;

    private final byte[] favicon;

    private final long time;

    public BrowserDestination(@NonNull String url, String title, byte[] favicon, long time) {
        this.url = url;
        this.title = title;
        this.favicon = favicon;
        this.time = time;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public byte[] getFavicon() {
        return favicon;
    }

    public long getTime() {
        return time;
    }

}