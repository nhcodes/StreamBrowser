package codes.nh.webvideobrowser.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import codes.nh.webvideobrowser.screens.bookmark.Bookmark;
import codes.nh.webvideobrowser.screens.browser.BrowserDestination;
import codes.nh.webvideobrowser.screens.stream.Stream;

@Database(
        entities = {Bookmark.class, Stream.class, BrowserDestination.class},
        version = 1,
        autoMigrations = {
                //@AutoMigration(from = 1, to = 2),
        })
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String NAME = "WebVideoBrowserDatabase";

    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            openDatabase(context);
        }
        return INSTANCE;
    }

    private synchronized static void openDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room
                    .databaseBuilder(context, AppDatabase.class, AppDatabase.NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
    }

    public abstract BookmarkDao getBookmarkDao();

    public abstract StreamHistoryDao getStreamHistoryDao();

    public abstract BrowserHistoryDao getBrowserHistoryDao();

}
