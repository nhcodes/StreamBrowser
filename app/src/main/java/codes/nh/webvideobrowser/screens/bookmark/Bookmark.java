package codes.nh.webvideobrowser.screens.bookmark;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Bookmark {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String url;

    private String title;

    private final byte[] favicon;

    public Bookmark(String url, String title, byte[] favicon) {
        this.url = url;
        this.title = title;
        this.favicon = favicon;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public byte[] getFavicon() {
        return favicon;
    }

}
