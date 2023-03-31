package codes.nh.webvideobrowser.fragments.history;

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
import codes.nh.webvideobrowser.fragments.browser.BrowserRequest;
import codes.nh.webvideobrowser.fragments.browser.BrowserViewModel;
import codes.nh.webvideobrowser.fragments.sheet.SheetFragment;
import codes.nh.webvideobrowser.fragments.sheet.SheetRequest;
import codes.nh.webvideobrowser.fragments.stream.Stream;
import codes.nh.webvideobrowser.fragments.stream.StreamInfoFragment;
import codes.nh.webvideobrowser.fragments.stream.StreamViewModel;
import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.RecyclerAdapter;

public class HistoryFragment extends SheetFragment {

    public HistoryFragment() {
        super(R.layout.fragment_history, R.string.navigation_title_history);
        AppUtils.log("init HistoryFragment");
    }

    private BrowserViewModel browserViewModel;

    private HistoryViewModel historyViewModel;

    private MainViewModel mainViewModel;

    private StreamViewModel streamViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HistoryAdapter historyAdapter = new HistoryAdapter();

        browserViewModel = new ViewModelProvider(requireActivity()).get(BrowserViewModel.class);

        historyViewModel = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);
        historyViewModel.getHistoryList().observe(getViewLifecycleOwner(), streams -> {
            historyAdapter.set(streams);
        });

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        streamViewModel = new ViewModelProvider(requireActivity()).get(StreamViewModel.class);

        FloatingActionButton clearButton = view.findViewById(R.id.fragment_history_button_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearHistory();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        RecyclerView listView = view.findViewById(R.id.fragment_history_list);
        listView.setAdapter(historyAdapter);
        listView.setLayoutManager(layoutManager);

        historyAdapter.setListener(new RecyclerAdapter.Listener<>() {
            @Override
            public void onClick(Stream stream) {
                openInfoFragment(stream);
            }

            @Override
            public void onLongClick(Stream stream) {
                browserViewModel.setRequestLoadUrl(new BrowserRequest(stream.getSourceUrl()));
            }
        });
    }

    private void openInfoFragment(Stream stream) {
        streamViewModel.setInfoStream(stream);
        SheetRequest request = new SheetRequest(StreamInfoFragment.class);
        mainViewModel.openSheet(request);
    }

    //database

    private void removeHistory(Stream stream) {
        historyViewModel.removeHistory(stream, success -> {
            String text = success ? "remove success" : "remove error";
            //mainViewModel.showSnackbar(text);
        });
    }

    private void clearHistory() {
        historyViewModel.clearHistory(success -> {
            String text = success ? "clear success" : "clear error";
            //mainViewModel.showSnackbar(text);
        });
    }
}