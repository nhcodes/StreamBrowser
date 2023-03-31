package codes.nh.webvideobrowser.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import codes.nh.webvideobrowser.fragments.stream.Stream;

@Dao
public interface StreamHistoryDao {

    @Query("SELECT * FROM Stream")
    LiveData<List<Stream>> getAll();

    @Query("SELECT * FROM Stream WHERE streamUrl = :url")
    List<Stream> getByUrl(String url);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Stream stream);

    @Query("UPDATE Stream SET startTime = :startTime WHERE streamUrl = :url")
    int update(String url, long startTime);

    @Delete
    int delete(Stream stream);

    @Query("DELETE FROM Stream")
    int deleteAll();

}
