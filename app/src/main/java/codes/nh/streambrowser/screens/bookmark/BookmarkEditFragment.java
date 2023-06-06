package codes.nh.streambrowser.screens.bookmark;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import codes.nh.streambrowser.R;
import codes.nh.streambrowser.screens.main.MainViewModel;
import codes.nh.streambrowser.screens.sheet.SheetFragment;
import codes.nh.streambrowser.utils.AppUtils;

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

        TextInputEditText titleInput = view.findViewById(R.id.fragment_bookmark_edit_input_title);
        titleInput.setText(bookmark.getTitle());

        TextInputEditText urlInput = view.findViewById(R.id.fragment_bookmark_edit_input_url);
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

    private void close() {
        mainViewModel.goBackToPreviousSheet();
    }

    //database

    private void updateBookmark(Bookmark bookmark, String url, String title) {
        bookmark.setUrl(url);
        bookmark.setTitle(title);
        bookmarkViewModel.updateBookmark(bookmark, changed -> {
        });
        close();
    }

    private void removeBookmark(Bookmark bookmark) {
        bookmarkViewModel.removeBookmark(bookmark, changed -> {
        });
        close();
    }

}
