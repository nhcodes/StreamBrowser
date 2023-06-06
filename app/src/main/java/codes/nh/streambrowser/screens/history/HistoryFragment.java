package codes.nh.streambrowser.screens.history;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import codes.nh.streambrowser.R;
import codes.nh.streambrowser.screens.browser.BrowserRequest;
import codes.nh.streambrowser.screens.browser.BrowserViewModel;
import codes.nh.streambrowser.screens.main.MainViewModel;
import codes.nh.streambrowser.screens.sheet.SheetFragment;
import codes.nh.streambrowser.screens.sheet.SheetRequest;
import codes.nh.streambrowser.screens.stream.Stream;
import codes.nh.streambrowser.screens.stream.StreamInfoFragment;
import codes.nh.streambrowser.screens.stream.StreamViewModel;
import codes.nh.streambrowser.utils.AppUtils;
import codes.nh.streambrowser.utils.RecyclerAdapter;

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
        clearButton.setOnClickListener(v -> clearHistory());

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
                openStreamSourceUrl(stream);
            }
        });
    }

    private void openInfoFragment(Stream stream) {
        streamViewModel.setInfoStream(stream);
        SheetRequest request = new SheetRequest(StreamInfoFragment.class);
        mainViewModel.openSheet(request);
    }

    private void openStreamSourceUrl(Stream stream) {
        browserViewModel.setRequestLoadUrl(new BrowserRequest(stream.getSourceUrl()));
    }

    private void clearHistory() {
        historyViewModel.clearHistory(changed -> {
        });
    }

}
