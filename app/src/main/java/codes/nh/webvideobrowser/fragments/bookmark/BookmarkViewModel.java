package codes.nh.webvideobrowser.fragments.bookmark;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.function.Consumer;

import codes.nh.webvideobrowser.database.AppDatabase;
import codes.nh.webvideobrowser.database.BookmarkDao;
import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.Async;

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

    public LiveData<List<Bookmark>> getBookmarkList() {
        return bookmarkList;
    }

    public void addBookmark(Bookmark bookmark, Consumer<Boolean> callback) {
        Async.execute(
                () -> bookmarkDao.insert(bookmark),
                (row) -> callback.accept(row != -1)
        );
    }

    public void updateBookmark(Bookmark bookmark, Consumer<Boolean> callback) {
        Async.execute(
                () -> bookmarkDao.update(bookmark),
                (changed) -> callback.accept(changed > 0)
        );
    }

    public void removeBookmark(Bookmark bookmark, Consumer<Boolean> callback) {
        Async.execute(
                () -> bookmarkDao.delete(bookmark),
                (changed) -> callback.accept(changed > 0)
        );
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
