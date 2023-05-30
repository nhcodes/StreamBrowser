package codes.nh.webvideobrowser.fragments.stream;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.Player;
import androidx.media3.ui.PlayerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import codes.nh.webvideobrowser.MainViewModel;
import codes.nh.webvideobrowser.PlayerViewModel;
import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.VideoActivity;
import codes.nh.webvideobrowser.fragments.sheet.SheetFragment;
import codes.nh.webvideobrowser.fragments.sheet.SheetRequest;
import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.Async;
import codes.nh.webvideobrowser.utils.SnackbarRequest;
import codes.nh.webvideobrowser.utils.UrlUtils;

public class StreamInfoFragment extends SheetFragment {

    public StreamInfoFragment() {
        super(R.layout.fragment_stream_info, R.string.fragment_stream_info_title, true);
        AppUtils.log("init StreamInfoFragment");
    }

    private MainViewModel mainViewModel;

    private PlayerViewModel playerViewModel;

    private StreamViewModel streamViewModel;

    private PlayerView video;

    private Stream stream;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);

        streamViewModel = new ViewModelProvider(requireActivity()).get(StreamViewModel.class);

        stream = streamViewModel.getInfoStream();

        video = view.findViewById(R.id.fragment_stream_info_video);
        video.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        video.setControllerAutoShow(true);
        video.setShowSubtitleButton(true);
        video.setFullscreenButtonClickListener(isFullScreen -> {
            play(stream);
        });

        BottomNavigationView actionbar = view.findViewById(R.id.fragment_stream_info_actionbar);
        actionbar.setSelectedItemId(R.id.action_none);
        actionbar.setOnItemSelectedListener(item -> {
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
        });

        checkStreamVariants(stream.getStreamUrl(), stream.getHeaders());

        AppUtils.log("URL IS NOW " + stream.getStreamUrl());
    }

    @Override
    public void onStart() {
        super.onStart();

        Player player = playerViewModel.start(stream);
        video.setPlayer(player);
    }

    @Override
    public void onStop() {
        super.onStop();

        playerViewModel.stop();
    }

    private void share(Stream stream) {
        AppUtils.openShareDialog(requireContext(), stream.getStreamUrl());
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
        intent.putExtra("stream", stream.toJson().toString());
        startActivity(intent);
    }

    //hls playlist variants

    private void checkStreamVariants(String url, Map<String, String> headers) {
        Async.execute(
                () -> {
                    try {

                        HttpURLConnection connection = UrlUtils.connectToUrl(url, headers);

                        boolean success = String.valueOf(connection.getResponseCode()).startsWith("2");
                        boolean cors = connection.getHeaderField("Access-Control-Allow-Origin").contains("*");
                        AppUtils.log("success=" + success + ", cors=" + cors);

                        List<Variant> hlsVariants = readHlsMultivariantPlaylist(
                                connection.getInputStream(),
                                UrlUtils.getAddressWithoutFileName(stream.getStreamUrl())
                        );
                        return hlsVariants;

                    } catch (Exception e) {
                        AppUtils.log("checkStreamVariants()", e);
                        return new ArrayList<Variant>();
                    }
                },
                hlsVariants -> {
                    if (!hlsVariants.isEmpty()) {
                        openHlsVariantChooserDialog(hlsVariants, variant -> {
                            Stream variantStream = new Stream(
                                    variant.url,
                                    stream.getSourceUrl(),
                                    stream.getTitle(),
                                    stream.getHeaders(),
                                    stream.getThumbnailUrls(),
                                    stream.getSubtitleUrls(),
                                    stream.getStartTime()
                            );
                            streamViewModel.setInfoStream(variantStream);
                            mainViewModel.openSheet(new SheetRequest(StreamInfoFragment.class));
                        });
                    }
                }, 5000L);
    }


    private void openHlsVariantChooserDialog(List<Variant> variants, Consumer<Variant> onChoose) {
        CharSequence[] items = variants.stream().map(variant ->
                variant.resolution + "\n" + variant.info
        ).toArray(CharSequence[]::new);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Choose stream")
                .setItems(items, (dialog, index) -> {
                    Variant variant = variants.get(index);
                    onChoose.accept(variant);
                    dialog.dismiss();
                })
                .show();
    }

    //https://developer.apple.com/documentation/http-live-streaming/creating-a-multivariant-playlist
    private List<Variant> readHlsMultivariantPlaylist(InputStream inputStream, String directoryUrl) {
        List<Variant> variants = new ArrayList<>();

        try (InputStreamReader streamReader = new InputStreamReader(inputStream);
             BufferedReader reader = new BufferedReader(streamReader);) {

            String headerPrefix = "#EXTM3U";
            String firstLine = reader.readLine();
            if (!firstLine.equals(headerPrefix)) {
                return variants;
            }

            String variantPrefix = "#EXT-X-STREAM-INF:";
            int variantPrefixLength = variantPrefix.length();

            Pattern resolutionPattern = Pattern.compile("RESOLUTION=(\\d+x\\d+)");

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(variantPrefix)) {
                    continue;
                }

                Variant variant = new Variant();

                String info = line.substring(variantPrefixLength);
                variant.info = info;

                Matcher matcher = resolutionPattern.matcher(info);
                if (matcher.find()) {
                    variant.resolution = matcher.group(1);
                }

                String url = reader.readLine();
                if (!url.startsWith("http")) {
                    url = directoryUrl + url;
                }
                variant.url = url;

                variants.add(variant);
            }

        } catch (IOException e) {
            AppUtils.log("readHlsMultivariantPlaylist()", e);
        }

        return variants;
    }

    private static class Variant {
        String url, info, resolution;
    }

}
