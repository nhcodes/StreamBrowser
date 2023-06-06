package codes.nh.streambrowser.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import codes.nh.streambrowser.screens.stream.Stream;

@Dao
public interface StreamHistoryDao {

    @Query("SELECT * FROM Stream")
    LiveData<List<Stream>> getAll();

    @Query("SELECT * FROM Stream WHERE streamUrl = :url")
    List<Stream> getByUrl(String url);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Stream stream);

    @Query("DELETE FROM Stream")
    int deleteAll();

}
