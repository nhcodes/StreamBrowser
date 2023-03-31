package codes.nh.webvideobrowser.fragments.stream;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.ui.PlayerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import codes.nh.webvideobrowser.MainViewModel;
import codes.nh.webvideobrowser.MediaPlayer;
import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.VideoActivity;
import codes.nh.webvideobrowser.fragments.sheet.SheetFragment;
import codes.nh.webvideobrowser.fragments.sheet.SheetRequest;
import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.SnackbarRequest;

public class StreamInfoFragment extends SheetFragment {

    public StreamInfoFragment() {
        super(R.layout.fragment_stream_info, R.string.fragment_stream_info_title, true);
        AppUtils.log("init StreamInfoFragment");
    }

    private MainViewModel mainViewModel;

    private StreamViewModel streamViewModel;

    private PlayerView video;

    private Stream stream;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        streamViewModel = new ViewModelProvider(requireActivity()).get(StreamViewModel.class);

        stream = streamViewModel.getInfoStream();

        video = view.findViewById(R.id.fragment_stream_info_video);
        video.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        video.setControllerAutoShow(true);
        video.setShowSubtitleButton(true);

        BottomNavigationView actionbar = view.findViewById(R.id.fragment_stream_info_actionbar);
        actionbar.setSelectedItemId(R.id.action_none);
        actionbar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_stream_share) {
                    share(stream);
                } else if (id == R.id.action_stream_download) {
                    download(stream);
                } else if (id == R.id.action_stream_cast) {
                    cast(stream);
                } else if (id == R.id.action_stream_play) {
                    play(stream);
                }

                return false;
            }
        });

            /*
            Async.execute(new Async.ResultTask<UrlUtils.HttpResponse>() {

                @Override
                public UrlUtils.HttpResponse doAsync() {
                    try {
                        return UrlUtils.readUrl(stream.getStreamUrl(), stream.getHeaders());
                    } catch (IOException e) {
                        AppUtils.log("read stream info", e);
                        return null;
                    }
                }

                @Override
                public void doSync(UrlUtils.HttpResponse response) {

                    loader.setVisibility(View.GONE);

                    if (response == null) {
                        //sheetViewModel.open(new SheetRequest(StreamsFragment.class));
                        return;
                    }

                    boolean willWork = String.valueOf(response.code).startsWith("2")
                            && response.headers.getOrDefault("Access-Control-Allow-Origin", "").contains("*");
                    useProxyCheck.setChecked(!willWork);

                    List<Variant> hlsVariants = readHlsMultivariantPlaylist(response.body, UrlUtils.getAddressWithoutFileName(stream.getStreamUrl()));
                    if (!hlsVariants.isEmpty()) {
                        openHlsVariantsDialog(stream, hlsVariants);
                    }

                }
            }, 5000L);*/
    }

    private void share(Stream stream) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, stream.getStreamUrl());
        startActivity(Intent.createChooser(intent, null));
    }

    private void download(Stream stream) {
        AppUtils.downloadFile(getApplicationContext(), stream.getStreamUrl(), stream.getHeaders());
        mainViewModel.showSnackbar(new SnackbarRequest(getString(R.string.fragment_stream_info_snackbar_download_started)));
    }

    private void cast(Stream stream) {
        mainViewModel.closeSheet();

        StreamRequest request = new StreamRequest(stream);
        streamViewModel.play(request);
    }

    private void play(Stream stream) {
        mainViewModel.closeSheet();

        Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
        intent.putExtra("url", stream.getStreamUrl());
        intent.putExtra("headers", AppUtils.mapToJson(stream.getHeaders()).toString());
        startActivity(intent);
    }

    private final MediaPlayer player = new MediaPlayer();

    @Override
    public void onStart() {
        super.onStart();

        player.start(getApplicationContext(), stream);
        video.setPlayer(player.getPlayer());
    }

    @Override
    public void onStop() {
        super.onStop();

        player.stop();
    }

    @Override
    public Runnable getBackButtonClickListener() {
        return () -> {
            mainViewModel.openSheet(new SheetRequest(StreamsFragment.class));
        };
    }

    /*

    //hls playlist

    private void openHlsVariantsDialog(Stream stream, List<Variant> variants) {
        CharSequence[] items = variants.stream().map(v -> v.resolution + "\n" + v.info).toArray(CharSequence[]::new);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Choose stream")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = variants.get(which).url; //todo improve
                        Stream newStream = stream.clone(url);
                        streamViewModel.setInfoStream(newStream);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //https://developer.apple.com/documentation/http_live_streaming/example_playlists_for_http_live_streaming/creating_a_multivariant_playlist
    private List<Variant> readHlsMultivariantPlaylist(byte[] content, String directoryUrl) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(content);
             InputStreamReader isr = new InputStreamReader(bais);
             BufferedReader br = new BufferedReader(isr);) {

            List<Variant> variants = new ArrayList<>();

            String headerPrefix = "#EXTM3U";
            int headerPrefixLength = headerPrefix.length();
            char[] buffer = new char[headerPrefixLength];
            br.read(buffer, 0, headerPrefixLength);
            if (!new String(buffer).equals(headerPrefix)) {
                return variants;
            }

            String prefix = "#EXT-X-STREAM-INF:";
            int prefixLength = prefix.length();

            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(prefix)) {
                    continue;
                }

                Variant variant = new Variant();

                String info = line.substring(prefixLength);
                variant.info = info;

                Pattern pattern = Pattern.compile("RESOLUTION=(\\d+x\\d+)");
                Matcher matcher = pattern.matcher(info);
                if (matcher.find()) {
                    variant.resolution = matcher.group(1);
                }

                String url = br.readLine();
                if (!url.startsWith("http")) {
                    url = directoryUrl + url;
                }
                variant.url = url;

                variants.add(variant);
            }

            return variants;
        } catch (IOException e) {
            AppUtils.log("readHlsMultivariantPlaylist()", e);
            return new ArrayList<>();
        }
    }

    private static class Variant {
        String url, info, resolution;
    }

    */

}
