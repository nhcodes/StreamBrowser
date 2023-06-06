package codes.nh.streambrowser.screens.stream;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;

import codes.nh.streambrowser.R;
import codes.nh.streambrowser.screens.browser.BrowserViewModel;
import codes.nh.streambrowser.screens.main.MainViewModel;
import codes.nh.streambrowser.screens.sheet.SheetFragment;
import codes.nh.streambrowser.screens.sheet.SheetRequest;
import codes.nh.streambrowser.utils.AppUtils;
import codes.nh.streambrowser.utils.RecyclerAdapter;

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
        streamFileButton.setOnClickListener(v -> {
            openFilePicker();
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

    private ActivityResultLauncher<String> launcher = null;

    private void openFilePicker() {
        launcher = AppUtils.registerActivityResultLauncher(
                requireActivity(),
                new ActivityResultContracts.GetContent(), uri -> {
                    playLocalFile(uri);
                    launcher.unregister();
                    launcher = null;
                }
        );
        String mimeType = "*/*";
        launcher.launch(mimeType);
    }

    private void playLocalFile(Uri uri) {
        if (uri == null) return;
        String fileName = AppUtils.getFileNameFromUri(getApplicationContext(), uri);
        Stream stream = new Stream(
                uri.toString(),
                uri.toString(),
                fileName,
                new HashMap<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                0L
        );
        stream.setUseProxy(true);
        //streamViewModel.play(new StreamRequest(stream));
        openInfoFragment(stream);
    }

}
