package codes.nh.webvideobrowser.fragments.bookmark;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import codes.nh.webvideobrowser.MainViewModel;
import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.fragments.sheet.SheetFragment;
import codes.nh.webvideobrowser.fragments.sheet.SheetRequest;
import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.SnackbarRequest;

public class BookmarkEditFragment extends SheetFragment {

    public BookmarkEditFragment() {
        super(R.layout.fragment_bookmark_edit, R.string.fragment_bookmark_edit_title, true);
        AppUtils.log("init BookmarkEditFragment");
    }

    private BookmarkViewModel bookmarkViewModel;

    private MainViewModel mainViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bookmarkViewModel = new ViewModelProvider(requireActivity()).get(BookmarkViewModel.class);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        Bookmark bookmark = bookmarkViewModel.getEditBookmark();

        TextInputLayout titleInputLayout = view.findViewById(R.id.fragment_bookmark_edit_input_title);
        EditText titleInput = titleInputLayout.getEditText();
        titleInput.setText(bookmark.getTitle());

        TextInputLayout urlInputLayout = view.findViewById(R.id.fragment_bookmark_edit_input_url);
        EditText urlInput = urlInputLayout.getEditText();
        urlInput.setText(bookmark.getUrl());

        MaterialButton removeButton = view.findViewById(R.id.fragment_bookmark_edit_button_remove);
        removeButton.setOnClickListener(v ->
                removeBookmark(bookmark)
        );

        MaterialButton saveButton = view.findViewById(R.id.fragment_bookmark_edit_button_save);
        saveButton.setOnClickListener(v ->
                updateBookmark(bookmark, urlInput.getText().toString(), titleInput.getText().toString())
        );
    }

    @Override
    public Runnable getBackButtonClickListener() {
        return () -> close();
    }

    private void close() {
        mainViewModel.openSheet(new SheetRequest(BookmarksFragment.class));
    }

    //database

    private void updateBookmark(Bookmark bookmark, String url, String title) {
        bookmark.setUrl(url);
        bookmark.setTitle(title);
        bookmarkViewModel.updateBookmark(bookmark, success -> {
            if (!success) mainViewModel.showSnackbar(new SnackbarRequest("update error"));
        });
        close();
    }

    private void removeBookmark(Bookmark bookmark) {
        bookmarkViewModel.removeBookmark(bookmark, success -> {
            if (!success) mainViewModel.showSnackbar(new SnackbarRequest("remove error"));
        });
        close();
    }
}
