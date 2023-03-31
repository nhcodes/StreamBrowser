package codes.nh.webvideobrowser;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.mediarouter.app.MediaRouteButton;

import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import codes.nh.webvideobrowser.fragments.bookmark.BookmarksFragment;
import codes.nh.webvideobrowser.fragments.browser.BrowserViewModel;
import codes.nh.webvideobrowser.fragments.cast.CastManager;
import codes.nh.webvideobrowser.fragments.cast.CastPlaybackFragment;
import codes.nh.webvideobrowser.fragments.history.HistoryFragment;
import codes.nh.webvideobrowser.fragments.history.HistoryViewModel;
import codes.nh.webvideobrowser.fragments.settings.SettingsFragment;
import codes.nh.webvideobrowser.fragments.sheet.SheetManager;
import codes.nh.webvideobrowser.fragments.sheet.SheetRequest;
import codes.nh.webvideobrowser.fragments.stream.Stream;
import codes.nh.webvideobrowser.fragments.stream.StreamViewModel;
import codes.nh.webvideobrowser.fragments.stream.StreamsFragment;
import codes.nh.webvideobrowser.proxy.ProxyService;
import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.FilePicker;
import codes.nh.webvideobrowser.utils.SnackbarRequest;

public class HomeActivity extends AppCompatActivity {

    /* TODO LIST

    https://developers.google.com/cast/docs/reference/web_receiver

    name castmaster

    implement all dialogs

    only view stuff and click listeners in fragments & activity

    make sure wifi is on

    improve hls variants

    implement cast devices

    fix viewmodel saved values, on rotate they get triggered (observed) again

    fragment listeners all one liner, move logic out

    //SSLServerSocketFactory.getDefault().createServerSocket(1111).

    cleanup hls proxy (make proxy & hls standalone?)

    */

    private BrowserViewModel browserViewModel;

    private HistoryViewModel historyViewModel;

    private MainViewModel mainViewModel;

    private StreamViewModel streamViewModel;

    private SheetManager sheetManager;

    private BottomNavigationView bottomNavigation;

    private View rootView;

    private MediaRouteButton mediaRouteButton;

    private FloatingActionButton streamsButton;

    //private CastManager castManager;

    private FilePicker filePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        AppUtils.log("onCreate HomeActivity");

        browserViewModel = new ViewModelProvider(this).get(BrowserViewModel.class);
        browserViewModel.getStreams().observe(this, streams -> {
            updateStreamsAvailable(streams.size());
        });

        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getCurrentSheet().observe(this, sheet -> {
            if (sheet == null) {
                sheetManager.close();
            } else {
                sheetManager.open(sheet);
            }
        });
        mainViewModel.getSnackbarMessage().observe(this, request -> {
            Snackbar snackbar = Snackbar.make(rootView, request.getMessage(), Snackbar.LENGTH_SHORT);
            if (request.getAction() != null) {
                snackbar.setAction(request.getAction().getMessage(), view -> request.getAction().getAction().run());
            }
            snackbar.show();
        });

        streamViewModel = new ViewModelProvider(this).get(StreamViewModel.class);
        streamViewModel.getStreamRequest().observe(this, streamRequest -> {
            if (streamRequest != null) {
                AppUtils.log("getStreamRequest");
                playStream(streamRequest.getStream());
                streamViewModel.play(null);
            }
        });

        rootView = findViewById(R.id.activity_home_layout_root);

        sheetManager = new SheetManager(this);

        /*App app = (App) getApplication();
        castManager = app.getCastManager();
        castManager.setListener(castListener);*/

        mediaRouteButton = findViewById(R.id.activity_home_button_mediaroute);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mediaRouteButton);
        mediaRouteButton.setClickable(false);

        streamsButton = findViewById(R.id.activity_home_button_streams);
        streamsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SheetRequest request = new SheetRequest(StreamsFragment.class);
                mainViewModel.openSheet(request);
            }
        });

        ViewGroup miniControllerFragment = findViewById(R.id.activity_home_fragment_minicontroller);
        for (int i = 0; i < miniControllerFragment.getChildCount(); i++) {
            View child = miniControllerFragment.getChildAt(i);
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SheetRequest request = new SheetRequest(CastPlaybackFragment.class);
                    mainViewModel.openSheet(request);
                }
            });
        }

        bottomNavigation = findViewById(R.id.activity_home_navigation_bottom);
        clearNavigationSelection();
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                SheetRequest request;
                if (id == R.id.action_navigation_bookmarks) {
                    request = new SheetRequest(BookmarksFragment.class);
                } else if (id == R.id.action_navigation_history) {
                    request = new SheetRequest(HistoryFragment.class);
                    //} else if (id == R.id.action_navigation_help) {
                    //request = new SheetRequest(HelpFragment.class);
                } else if (id == R.id.action_navigation_settings) {
                    request = new SheetRequest(SettingsFragment.class);
                } else {
                    return false;
                }
                mainViewModel.openSheet(request);
                return true;
            }
        });

        getOnBackPressedDispatcher().addCallback(backPressedCallback);

        //castManager.startSessionListener();

        filePicker = new FilePicker(this);
        filePicker.register(this::onPickFile);
    }

    @Override
    protected void onDestroy() {
        AppUtils.log("onDestroy HomeActivity");

        backPressedCallback.remove();

        /*if (!castManager.isPlaying()) { //todo
            ProxyService.stop(getApplicationContext());
        }

        castManager.stopSessionListener();*/

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //castManager.startPlaybackListener();
    }

    @Override
    protected void onPause() {
        //castManager.stopPlaybackListener();

        super.onPause();
    }

    //navigation

    public void clearNavigationSelection() {
        bottomNavigation.setSelectedItemId(R.id.action_navigation_divider);
    }

    //streams available

    private int[] buttonColors = null;

    private void updateStreamsAvailable(int count) {
        if (buttonColors == null) {
            TypedArray colors = obtainStyledAttributes(new int[]{R.attr.colorPrimary, R.attr.colorSurface});
            buttonColors = new int[]{colors.getColor(0, -1), colors.getColor(1, -1)};
            colors.recycle();
        }
        int color = count > 0 ? buttonColors[0] : buttonColors[1];
        streamsButton.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    //back button

    private long lastClickedBack = System.currentTimeMillis();

    private final OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {

            if (mainViewModel.isSheetOpen()) {
                mainViewModel.closeSheet();
                return;
            }

            boolean success = browserViewModel.goBack();
            if (success) return;

            int delay = getResources().getInteger(R.integer.back_click_close_delay);

            long now = System.currentTimeMillis();
            if (now - lastClickedBack < delay) {
                finishAffinity();
                return;
            }

            lastClickedBack = now;
            mainViewModel.showSnackbar(new SnackbarRequest(getString(R.string.toast_close_app)));

        }
    };

    //stream

    public void playStream(Stream stream) {
        historyViewModel.getHistory(stream.getStreamUrl(), historyList -> {

            if (historyList.isEmpty()) {
                startStream(stream);
                return;
            }

            Stream history = historyList.get(0);
            if (history.getStartTime() < 1000) {
                startStream(stream);
                return;
            }

            stream.setStartTime(history.getStartTime());
            openStreamResumeDialog(stream);

        });
    }

    private void openStreamResumeDialog(Stream stream) {
        String time = AppUtils.millisToMinutesSeconds(stream.getStartTime());
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_stream_resume_title)
                .setMessage(getString(R.string.dialog_stream_resume_message, time))
                .setNeutralButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stream.setStartTime(0);
                        startStream(stream);
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startStream(stream);
                    }
                })
                .show();
    }

    private void startStream(Stream stream) {
        //boolean connected = castManager.playStream(stream);
        boolean connected = new CastMediaPlayer().start(getApplicationContext(), stream);
        if (!connected) {
            mediaRouteButton.showDialog();
        }
    }

    private final CastManager.Listener castListener = new CastManager.Listener() {

        @Override
        public void onStreamRequested(Stream stream) {

            mainViewModel.showSnackbar(new SnackbarRequest("onStreamRequested"));

        }

        @Override
        public void onStreamStart(Stream stream, String error) {
            if (error != null) {
                mainViewModel.closeSheet();
                //todo mainViewModel.showSnackbar(new SnackbarRequest(getString(R.string.snackbar_play_error_message, error)));

                if (!stream.useProxy()) {
                    stream.setUseProxy(true);

                    ProxyService.start(getApplicationContext()); //todo improve
                    Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                        playStream(stream);
                    }, 1, TimeUnit.SECONDS);

                } else {
                    mainViewModel.showSnackbar(new SnackbarRequest("error casting: " + error));
                }

                return;
            }

            SheetRequest request = new SheetRequest(CastPlaybackFragment.class);
            mainViewModel.openSheet(request);

            mainViewModel.showSnackbar(new SnackbarRequest("onStreamStart"));

            historyViewModel.addHistory(stream, success -> {
                if (!success) {
                    AppUtils.log("history insert error");
                    //mainViewModel.showSnackbar("history insert error");
                }
            });
        }

        @Override
        public void onStreamUpdate(RemoteMediaClient remoteMediaClient, int stateId) {

            if (stateId == MediaStatus.PLAYER_STATE_IDLE) {
                //mainViewModel.closeSheet();
                return;
            }

            String streamUrl;
            try {
                streamUrl = remoteMediaClient.getMediaInfo().getCustomData().getString("url"); //bc of proxy
            } catch (JSONException e) {
                AppUtils.log("custom data json", e);
                return;
            }

            long streamPosition = remoteMediaClient.isLiveStream() ? -1 : remoteMediaClient.getApproximateStreamPosition();

            historyViewModel.updateHistory(streamUrl, streamPosition, success -> {
                if (!success) {
                    AppUtils.log("history update error");
                    //mainViewModel.showSnackbar("history update error");
                }
            });

        }

        @Override
        public void onSessionUpdate(CastManager.SessionStatus sessionStatus, Object... data) {
            //AppUtils.log("onSessionUpdate " + sessionStatus.name());
            if (sessionStatus == CastManager.SessionStatus.STARTED || sessionStatus == CastManager.SessionStatus.RESUMED) {
                //castManager.startPlaybackListener();
            } else if (sessionStatus == CastManager.SessionStatus.ENDED) {
                ProxyService.stop(getApplicationContext());
            }
        }

        @Override
        public void onReceiveMessage(String message) {
            mainViewModel.showSnackbar(new SnackbarRequest("Received: " + message));
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) { //clear edittext focus on click outside
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof EditText) {
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    view.clearFocus();
                    AppUtils.closeKeyboard(view);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    //file picker

    public FilePicker getFilePicker() {
        return filePicker;
    }

    public void onPickFile(Uri uri) {
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
        playStream(stream);
    }
}