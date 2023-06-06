package codes.nh.streambrowser.screens.bookmark;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.function.Consumer;

import codes.nh.streambrowser.database.AppDatabase;
import codes.nh.streambrowser.database.BookmarkDao;
import codes.nh.streambrowser.utils.AppUtils;
import codes.nh.streambrowser.utils.async.Async;

public class BookmarkViewModel extends AndroidViewModel {

    private final BookmarkDao bookmarkDao;

    private final LiveData<List<Bookmark>> bookmarkList;

    public BookmarkViewModel(Application application) {
        super(application);
        AppUtils.log("init BookmarkViewModel");

        AppDatabase database = AppDatabase.getInstance(application);
        bookmarkDao = database.getBookmarkDao();
        bookmarkList = bookmarkDao.getAll();
    }

    //bookmark database

    public LiveData<List<Bookmark>> getBookmarkList() {
        return bookmarkList;
    }

    public void addBookmark(Bookmark bookmark, Consumer<Long> callback) {
        Async.execute(() -> bookmarkDao.insert(bookmark), callback);
    }

    public void updateBookmark(Bookmark bookmark, Consumer<Integer> callback) {
        Async.execute(() -> bookmarkDao.update(bookmark), callback);
    }

    public void removeBookmark(Bookmark bookmark, Consumer<Integer> callback) {
        Async.execute(() -> bookmarkDao.delete(bookmark), callback);
    }

    //edit bookmark

    private Bookmark editBookmark = null;

    public void setEditBookmark(Bookmark bookmark) {
        editBookmark = bookmark;
    }

    public Bookmark getEditBookmark() {
        return editBookmark;
    }

}
