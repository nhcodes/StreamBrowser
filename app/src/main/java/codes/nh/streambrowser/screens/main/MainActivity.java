package codes.nh.streambrowser.screens.main;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import codes.nh.streambrowser.R;
import codes.nh.streambrowser.screens.bookmark.BookmarksFragment;
import codes.nh.streambrowser.screens.browser.BrowserViewModel;
import codes.nh.streambrowser.screens.cast.CastFullControllerFragment;
import codes.nh.streambrowser.screens.cast.CastManager;
import codes.nh.streambrowser.screens.cast.CastViewModel;
import codes.nh.streambrowser.screens.help.HelpFragment;
import codes.nh.streambrowser.screens.history.HistoryFragment;
import codes.nh.streambrowser.screens.history.HistoryViewModel;
import codes.nh.streambrowser.screens.settings.SettingsFragment;
import codes.nh.streambrowser.screens.settings.SettingsManager;
import codes.nh.streambrowser.screens.sheet.SheetManager;
import codes.nh.streambrowser.screens.sheet.SheetRequest;
import codes.nh.streambrowser.screens.stream.Stream;
import codes.nh.streambrowser.screens.stream.StreamViewModel;
import codes.nh.streambrowser.screens.stream.StreamsFragment;
import codes.nh.streambrowser.utils.AppUtils;

public class MainActivity extends AppCompatActivity {

    private BrowserViewModel browserViewModel;

    private HistoryViewModel historyViewModel;

    private MainViewModel mainViewModel;

    private CastViewModel castViewModel;

    private StreamViewModel streamViewModel;

    private SheetManager sheetManager;

    private BottomNavigationView bottomNavigation;

    private View rootView;

    private FloatingActionButton streamsButton;

    //private FragmentContainerView miniControllerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppUtils.log("onCreate MainActivity");

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

        castViewModel = new ViewModelProvider(this).get(CastViewModel.class);
        castViewModel.getCastManager().setListener(castListener);

        streamViewModel = new ViewModelProvider(this).get(StreamViewModel.class);
        streamViewModel.getStreamRequest().observe(this, streamRequest -> {
            if (streamRequest != null) {
                AppUtils.log("getStreamRequest");
                startStream(streamRequest.getStream());
                streamViewModel.play(null);
            }
        });

        rootView = findViewById(R.id.activity_main_layout_root);

        sheetManager = new SheetManager(this);
        sheetManager.setListener(new SheetManager.Listener() {
            @Override
            public void onOpen(SheetRequest sheetRequest) {
                if (sheetRequest.getFragmentClass() == StreamsFragment.class) {
                    clearNavigationSelection();
                }
            }

            @Override
            public void onRequestGoBack() {
                mainViewModel.goBackToPreviousSheet();
            }

            @Override
            public void onRequestClose() {
                mainViewModel.closeSheet();
            }

            @Override
            public void onClosed() {
                clearNavigationSelection();
            }
        });

        streamsButton = findViewById(R.id.activity_main_button_streams);
        streamsButton.setOnClickListener(view -> {
            SheetRequest request = new SheetRequest(StreamsFragment.class);
            mainViewModel.openSheet(request);
        });

        /*miniControllerFragment = findViewById(R.id.activity_main_fragment_minicontroller);
        miniControllerFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SheetRequest request = new SheetRequest(CastFullControllerFragment.class);
                mainViewModel.openSheet(request);
            }
        });

        if (castViewModel.getCastManager().isPlaying()) {
            miniControllerFragment.setVisibility(View.VISIBLE);
        }*/

        bottomNavigation = findViewById(R.id.activity_main_navigation_bottom);
        clearNavigationSelection();
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            SheetRequest request;
            if (id == R.id.action_navigation_bookmarks) {
                request = new SheetRequest(BookmarksFragment.class);
            } else if (id == R.id.action_navigation_history) {
                request = new SheetRequest(HistoryFragment.class);
            } else if (id == R.id.action_navigation_help) {
                request = new SheetRequest(HelpFragment.class);
            } else if (id == R.id.action_navigation_settings) {
                request = new SheetRequest(SettingsFragment.class);
            } else {
                return false;
            }
            mainViewModel.openSheet(request);
            return true;
        });

        getOnBackPressedDispatcher().addCallback(backPressedCallback);

        castViewModel.getCastManager().startSessionListener();
    }

    @Override
    protected void onDestroy() {
        AppUtils.log("onDestroy MainActivity");

        backPressedCallback.remove();

        castViewModel.getCastManager().stopSessionListener();

        super.onDestroy();
    }

    //navigation

    public void clearNavigationSelection() {
        bottomNavigation.setSelectedItemId(R.id.action_navigation_divider);
    }

    //streams available

    private void updateStreamsAvailable(int count) {
        int primaryColor = MaterialColors.getColor(this, R.attr.colorPrimary, "colorPrimary missing");
        int surfaceColor = MaterialColors.getColor(this, R.attr.colorSurface, "colorSurface missing");
        int color = count > 0 ? primaryColor : surfaceColor;
        streamsButton.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    //back button

    private long lastClickedBack = System.currentTimeMillis();

    private final OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {

            if (mainViewModel.isSheetOpen()) {
                mainViewModel.goBackToPreviousSheet();
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

    /*
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
    */

    /*
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
    */

    private void startStream(Stream stream) {
        castViewModel.getCastManager().requestStream(this, stream);
    }

    private final CastManager.Listener castListener = new CastManager.Listener() {

        @Override
        public void onSessionUpdate(CastManager.SessionStatus sessionStatus, Object... data) {
            if (sessionStatus == CastManager.SessionStatus.STARTING) {
                mainViewModel.showSnackbar(new SnackbarRequest("starting cast session..."));
            }
        }

        @Override
        public void onPlaybackRequested(Stream stream) {
            mainViewModel.showSnackbar(new SnackbarRequest("playback requested..."));
        }

        @Override
        public void onPlaybackStarted(Stream stream, String error) {

            if (error != null) {
                mainViewModel.closeSheet(CastFullControllerFragment.class);

                mainViewModel.showSnackbar(new SnackbarRequest(getString(R.string.snackbar_play_error_message, error)));

                boolean proxyEnabled = new SettingsManager(getApplicationContext()).getUseProxy();
                if (!proxyEnabled || stream.useProxy()) { //if streaming through proxy is disabled or stream is already playing on proxy
                    return;
                }

                //try streaming through proxy
                stream.setUseProxy(true);
                startStream(stream);

                return;
            }

            SheetRequest request = new SheetRequest(CastFullControllerFragment.class);
            mainViewModel.openSheet(request);

            historyViewModel.addHistory(stream, rowId -> {
            });
        }

        @Override
        public void onPlaybackUpdate(RemoteMediaClient remoteMediaClient, int stateId) {

            if (stateId == MediaStatus.PLAYER_STATE_IDLE) {
                mainViewModel.closeSheet(CastFullControllerFragment.class);
                return;
            }

            Stream stream;
            try {
                stream = Stream.fromJson(remoteMediaClient.getMediaInfo().getCustomData());
            } catch (Exception e) {
                AppUtils.log("onPlaybackUpdate getMediaInfo().getCustomData()", e);
                return;
            }

            long streamPosition = remoteMediaClient.isLiveStream() ? -1 : remoteMediaClient.getApproximateStreamPosition();
            stream.setStartTime(streamPosition);

            historyViewModel.addHistory(stream, rowId -> { //todo updateHistory
            });

        }

        @Override
        public void onReceiveMessage(String message) {
            AppUtils.log("onMessageReceived: " + message);
        }
    };

    /*
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
    }*/

}