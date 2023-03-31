package codes.nh.webvideobrowser.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import codes.nh.webvideobrowser.fragments.bookmark.Bookmark;

@Dao
public interface BookmarkDao {

    @Query("SELECT * FROM Bookmark")
    LiveData<List<Bookmark>> getAll();

    @Insert
    long insert(Bookmark bookmark);

    @Update
    int update(Bookmark bookmark);

    @Delete
    int delete(Bookmark bookmark);

}
