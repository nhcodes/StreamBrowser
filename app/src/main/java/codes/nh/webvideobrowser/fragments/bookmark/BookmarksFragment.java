package codes.nh.webvideobrowser.fragments.bookmark;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import codes.nh.webvideobrowser.MainViewModel;
import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.fragments.browser.BrowserDestination;
import codes.nh.webvideobrowser.fragments.browser.BrowserRequest;
import codes.nh.webvideobrowser.fragments.browser.BrowserViewModel;
import codes.nh.webvideobrowser.fragments.sheet.SheetFragment;
import codes.nh.webvideobrowser.fragments.sheet.SheetRequest;
import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.RecyclerAdapter;
import codes.nh.webvideobrowser.utils.SnackbarRequest;

public class BookmarksFragment extends SheetFragment {

    public BookmarksFragment() {
        super(R.layout.fragment_bookmarks, R.string.navigation_title_bookmarks);
        AppUtils.log("init BookmarksFragment");
    }

    private BookmarkViewModel bookmarkViewModel;

    private BrowserViewModel browserViewModel;

    private MainViewModel mainViewModel;

    private RecyclerView listView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BookmarksAdapter bookmarksAdapter = new BookmarksAdapter();

        bookmarkViewModel = new ViewModelProvider(requireActivity()).get(BookmarkViewModel.class);
        bookmarkViewModel.getBookmarkList().observe(getViewLifecycleOwner(), bookmarks -> {
            bookmarksAdapter.set(bookmarks);
        });

        browserViewModel = new ViewModelProvider(requireActivity()).get(BrowserViewModel.class);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        FloatingActionButton addButton = view.findViewById(R.id.fragment_bookmarks_button_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BrowserDestination destination = browserViewModel.getDestination(0);
                if (destination == null) {
                    mainViewModel.showSnackbar(new SnackbarRequest("no website loaded"));
                    return;
                }
                Bookmark bookmark = new Bookmark(destination.getUrl(), destination.getTitle(), destination.getFavicon());
                addBookmark(bookmark);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        listView = view.findViewById(R.id.fragment_bookmarks_list);
        listView.setAdapter(bookmarksAdapter);
        listView.setLayoutManager(layoutManager);

        bookmarksAdapter.setListener(new RecyclerAdapter.Listener<>() {
            @Override
            public void onClick(Bookmark bookmark) {
                BrowserRequest request = new BrowserRequest(bookmark.getUrl());
                browserViewModel.setRequestLoadUrl(request);
                mainViewModel.closeSheet();
            }

            @Override
            public void onLongClick(Bookmark bookmark) {
                bookmarkViewModel.setEditBookmark(bookmark);
                SheetRequest request = new SheetRequest(BookmarkEditFragment.class);
                mainViewModel.openSheet(request);
            }
        });
    }

    //database

    private void addBookmark(Bookmark bookmark) {
        bookmarkViewModel.addBookmark(bookmark, success -> {
            String text = success ? "add success" : "add error";
            //mainViewModel.showSnackbar(text);
            listView.smoothScrollBy(0, Integer.MIN_VALUE);
        });
    }
}
