package codes.nh.webvideobrowser.fragments.stream;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import codes.nh.webvideobrowser.HomeActivity;
import codes.nh.webvideobrowser.MainViewModel;
import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.fragments.browser.BrowserViewModel;
import codes.nh.webvideobrowser.fragments.sheet.SheetFragment;
import codes.nh.webvideobrowser.fragments.sheet.SheetRequest;
import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.RecyclerAdapter;

public class StreamsFragment extends SheetFragment {

    public StreamsFragment() {
        super(R.layout.fragment_streams, R.string.navigation_title_streams);
        AppUtils.log("init StreamsFragment");
    }

    private BrowserViewModel browserViewModel;

    private MainViewModel mainViewModel;

    private StreamViewModel streamViewModel;

    private RecyclerView listView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        StreamsAdapter streamsAdapter = new StreamsAdapter();

        browserViewModel = new ViewModelProvider(requireActivity()).get(BrowserViewModel.class);
        browserViewModel.getStreams().observe(getViewLifecycleOwner(), streams -> {
            streamsAdapter.set(streams);
            listView.smoothScrollToPosition(Integer.MAX_VALUE);
        });

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        streamViewModel = new ViewModelProvider(requireActivity()).get(StreamViewModel.class);

        ExtendedFloatingActionButton streamFileButton = view.findViewById(R.id.fragment_streams_button_stream_file);
        streamFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //browserViewModel.clearStreams();
                HomeActivity activity = (HomeActivity) requireActivity();
                activity.getFilePicker().launch();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        listView = view.findViewById(R.id.fragment_streams_list);
        listView.setAdapter(streamsAdapter);
        listView.setLayoutManager(layoutManager);

        streamsAdapter.setListener(new RecyclerAdapter.Listener<>() {
            @Override
            public void onClick(Stream stream) {
                openInfoFragment(stream);
            }

            @Override
            public void onLongClick(Stream stream) {
                //openInfoFragment(stream);
            }
        });
    }

    private void openInfoFragment(Stream stream) {
        streamViewModel.setInfoStream(stream);
        SheetRequest request = new SheetRequest(StreamInfoFragment.class);
        mainViewModel.openSheet(request);
    }
}
