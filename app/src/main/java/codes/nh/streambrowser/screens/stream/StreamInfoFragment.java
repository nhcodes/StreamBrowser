package codes.nh.streambrowser.screens.stream;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.Player;
import androidx.media3.ui.PlayerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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

import codes.nh.streambrowser.R;
import codes.nh.streambrowser.screens.history.HistoryViewModel;
import codes.nh.streambrowser.screens.main.MainViewModel;
import codes.nh.streambrowser.screens.main.SnackbarRequest;
import codes.nh.streambrowser.screens.player.PlayerActivity;
import codes.nh.streambrowser.screens.player.PlayerViewModel;
import codes.nh.streambrowser.screens.sheet.SheetFragment;
import codes.nh.streambrowser.screens.sheet.SheetRequest;
import codes.nh.streambrowser.utils.AppUtils;
import codes.nh.streambrowser.utils.UrlUtils;
import codes.nh.streambrowser.utils.async.Async;

public class StreamInfoFragment extends SheetFragment {

    public StreamInfoFragment() {
        super(R.layout.fragment_stream_info, R.string.fragment_stream_info_title, true);
        AppUtils.log("init StreamInfoFragment");
    }

    private HistoryViewModel historyViewModel;

    private MainViewModel mainViewModel;

    private PlayerViewModel playerViewModel;

    private StreamViewModel streamViewModel;

    private PlayerView video;

    private Stream stream;

    private List<Variant> streamVariants = new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        historyViewModel = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);

        streamViewModel = new ViewModelProvider(requireActivity()).get(StreamViewModel.class);

        stream = streamViewModel.getInfoStream();

        historyViewModel.getHistory(stream.getStreamUrl(), historyList -> {
            if (historyList.isEmpty()) {
                return;
            }
            Stream history = historyList.get(0);
            stream.setStartTime(history.getStartTime());
        });

        video = view.findViewById(R.id.fragment_stream_info_video);
        video.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        video.setControllerAutoShow(true);
        video.setShowSubtitleButton(true);
        video.setFullscreenButtonClickListener(isFullScreen -> {
            play(stream);
        });

        BottomNavigationView actionbar = view.findViewById(R.id.fragment_stream_info_actionbar);
        //actionbar.setSelectedItemId(R.id.action_stream_play); //todo
        actionbar.setSelectedItemId(R.id.action_stream_none);
        actionbar.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_stream_resolutions) {
                openVariantsPopup(actionbar, streamVariants, variant -> {
                    openVariantStream(stream, variant);
                });
            } else if (id == R.id.action_stream_share) {
                share(stream);
            /*} else if (id == R.id.action_stream_download) {
                download(stream);*/
            } else if (id == R.id.action_stream_cast) {
                cast(stream);
            } else if (id == R.id.action_stream_play) {
                play(stream);
            }
            return false;
        });

        checkStreamVariants(stream.getStreamUrl(), stream.getHeaders(), variants -> {
            streamVariants = variants;
            boolean visible = !streamVariants.isEmpty();
            actionbar.getMenu().findItem(R.id.action_stream_resolutions).setVisible(visible);
        });
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

    private void openVariantStream(Stream stream, Variant variant) {
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

        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
        intent.putExtra("stream", stream.toJson().toString());
        startActivity(intent);
    }

    //hls playlist variants

    private static class Variant {
        String url, info, resolution;
    }

    private void checkStreamVariants(String url, Map<String, String> headers, Consumer<List<Variant>> callback) {
        Async.execute(
                () -> {
                    try {

                        HttpURLConnection connection = UrlUtils.connectToUrl(url, headers);

                        boolean success = String.valueOf(connection.getResponseCode()).startsWith("2");
                        String corsHeader = connection.getHeaderField("Access-Control-Allow-Origin");
                        boolean cors = corsHeader != null && corsHeader.contains("*");
                        AppUtils.log("success=" + success + ", cors=" + cors);

                        if (!UrlUtils.getFileNameFromUrl(url).contains(".m3u8")) {
                            return new ArrayList<Variant>();
                        }

                        return readHlsMultivariantPlaylist(
                                connection.getInputStream(),
                                UrlUtils.getAddressWithoutFileName(stream.getStreamUrl())
                        );

                    } catch (Exception e) {
                        AppUtils.log("checkStreamVariants()", e);
                        return new ArrayList<Variant>();
                    }
                },
                hlsVariants -> {
                    callback.accept(hlsVariants);
                },
                5000L
        );
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

    private void openVariantsPopup(View view, List<Variant> variants, Consumer<Variant> onSelect) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        Menu menu = popup.getMenu();

        int i = 0;
        for (Variant variant : variants) {
            String title = variant.resolution;// + " - " + variant.info;
            menu.add(Menu.NONE, i, i, title);
            i++;
        }

        popup.setOnMenuItemClickListener(item -> {
            Variant selectedVariant = variants.get(item.getItemId());
            onSelect.accept(selectedVariant);
            return true;
        });

        popup.show();
    }

}
