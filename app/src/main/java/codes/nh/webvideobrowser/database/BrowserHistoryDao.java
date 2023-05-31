package codes.nh.webvideobrowser.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import codes.nh.webvideobrowser.fragments.browser.BrowserDestination;

@Dao
public interface BrowserHistoryDao {

    @Query("SELECT * FROM BrowserDestination ORDER BY time DESC")
    LiveData<List<BrowserDestination>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BrowserDestination destination);

    @Query("DELETE FROM BrowserDestination WHERE time > :time")
    int deleteAfter(long time);

    @Query("DELETE FROM BrowserDestination WHERE url NOT IN (SELECT url from BrowserDestination ORDER BY time DESC LIMIT :keepCount)")
    int clear(int keepCount);

}
