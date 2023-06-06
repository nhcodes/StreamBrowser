package codes.nh.streambrowser.screens.browser;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.mediarouter.app.MediaRouteButton;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import codes.nh.streambrowser.R;
import codes.nh.streambrowser.screens.main.MainViewModel;
import codes.nh.streambrowser.screens.main.SnackbarRequest;
import codes.nh.streambrowser.screens.stream.Stream;
import codes.nh.streambrowser.utils.AppUtils;
import codes.nh.streambrowser.utils.ImageUtils;
import codes.nh.streambrowser.utils.UrlUtils;

public class BrowserFragment extends Fragment {

    public BrowserFragment() {
        super(R.layout.fragment_browser);
        AppUtils.log("init BrowserFragment");
    }

    private BrowserViewModel browserViewModel;

    private MainViewModel mainViewModel;

    private Browser webView;

    private LinearProgressIndicator progressBar;

    private TextInputEditText urlInput;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        browserViewModel = new ViewModelProvider(requireActivity()).get(BrowserViewModel.class);

        browserViewModel.getRequestLoadUrl().observe(getViewLifecycleOwner(), request -> {
            if (request != null) {
                AppUtils.log("getRequestLoadUrl");
                browserViewModel.setRequestLoadUrl(null);
                webView.loadUrl(request.getUrl(), request.getHeaders());
            }
        });

        browserViewModel.getDesktopMode().observe(getViewLifecycleOwner(), desktopMode -> {
            if (webView.getDesktopMode() != desktopMode) {
                AppUtils.log("getDesktopMode");
                webView.setDesktopMode(desktopMode);
                webView.reload();
            }
        });

        browserViewModel.getDestinationList().observe(getViewLifecycleOwner(), destinations -> {
            if (webView.getUrl() == null) {
                AppUtils.log("getDestinationList");
                String url = !destinations.isEmpty() ? destinations.get(0).getUrl() : "https://google.com/";
                browserViewModel.setRequestLoadUrl(new BrowserRequest(url));//webView.loadUrl(url);
            }
        });

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        webView = view.findViewById(R.id.fragment_browser_webview);
        webView.setDesktopMode(browserViewModel.getDesktopMode().getValue());
        webView.setListener(new Browser.Listener() {

            @Override
            public void onRequestPage(String url) {
                urlInput.setText(url);

                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStartLoadPage(String url) {
                urlInput.setText(url);

                progressBar.setProgress(0, false);
                progressBar.setVisibility(View.VISIBLE);

                browserViewModel.clearStreams();
            }

            @Override
            public void onFinishLoadPage(String url) {
                progressBar.setProgress(100, true);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onUpdateUrl(String url) {
                urlInput.setText(url);

                BrowserDestination destination = new BrowserDestination(url, webView.getTitle(), ImageUtils.bytesFromBitmap(webView.getFavicon()), System.currentTimeMillis());
                addDestination(destination);
            }

            @Override
            public void onUpdateTitle(String title) {
                BrowserDestination destination = new BrowserDestination(webView.getUrl(), title, ImageUtils.bytesFromBitmap(webView.getFavicon()), System.currentTimeMillis());
                addDestination(destination);
            }

            @Override
            public void onUpdateFavicon(Bitmap favicon) {
                BrowserDestination destination = new BrowserDestination(webView.getUrl(), webView.getTitle(), ImageUtils.bytesFromBitmap(favicon), System.currentTimeMillis());
                addDestination(destination);
            }

            @Override
            public void onUpdateProgress(int progress) {
                progressBar.setIndeterminate(false);
                progressBar.setProgress(progress);
            }

            @Override
            public boolean onRedirect(String oldUrl, String newUrl) {
                if (!browserViewModel.getBlockRedirects()) {
                    return true;
                }

                String domain = UrlUtils.getDomainNameFromURL(newUrl);

                SnackbarRequest snackbarRequest = new SnackbarRequest(
                        getString(R.string.snackbar_redirect_message, domain),
                        new SnackbarRequest.SnackbarAction(
                                getString(R.string.snackbar_redirect_action),
                                () -> {
                                    browserViewModel.setRequestLoadUrl(new BrowserRequest(newUrl)); //webView.loadUrl(newUrl);
                                }
                        )
                );
                mainViewModel.showSnackbar(snackbarRequest);

                return false;
            }

            @Override
            public void onFindStream(Stream stream) {
                browserViewModel.addStream(stream);
            }

            @Override
            public void onTouch() {
                if (urlInput.isFocused()) {
                    urlInput.clearFocus();

                    AppUtils.closeKeyboard(urlInput);
                }
            }
        });

        progressBar = view.findViewById(R.id.fragment_browser_loader);

        urlInput = view.findViewById(R.id.fragment_browser_input_url);
        urlInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                openUrl(v.getText().toString());
            }
            return false;
        });

        ImageButton backButton = view.findViewById(R.id.fragment_browser_button_back);
        backButton.setOnClickListener(v -> openBrowserHistoryDialog(v));

        MediaRouteButton mediaRouteButton = view.findViewById(R.id.media_route_button);
        CastButtonFactory.setUpMediaRouteButton(requireContext().getApplicationContext(), mediaRouteButton);

    }

    @Override
    public void onPause() {
        webView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
    }

    private void openUrl(String query) {
        if (!query.startsWith("https://") && !query.startsWith("http://")) {
            if (query.contains(" ") || !query.contains(".")) {
                query = "http://google.com/search?q=" + query.replace(" ", "+");
            } else {
                query = "http://" + query;
            }
        }
        urlInput.clearFocus();
        browserViewModel.setRequestLoadUrl(new BrowserRequest(query));//webView.loadUrl(query);
    }

    private void addDestination(BrowserDestination destination) {
        browserViewModel.addDestination(destination, rowId -> {
        });
    }

    private void openBrowserHistoryDialog(View view) {
        List<BrowserDestination> allDestinations = browserViewModel.getDestinationList().getValue();
        /*if (allDestinations.isEmpty()) {
            mainViewModel.showSnackbar("browser history empty");
            return;
        }*/
        List<BrowserDestination> destinations = allDestinations.subList(0, Math.min(5, allDestinations.size()));

        PopupMenu popup = new PopupMenu(view.getContext(), view);
        Menu menu = popup.getMenu();

        int i = 0;
        for (BrowserDestination destination : destinations) {
            String domainName = UrlUtils.getDomainNameFromURL(destination.getUrl());
            String text = domainName + " | " + destination.getTitle();
            menu.add(Menu.NONE, i, i, text);
            i++;
        }

        popup.setOnMenuItemClickListener(item -> {
            BrowserDestination destination = destinations.get(item.getItemId());
            browserViewModel.goBack(destination);
            return true;
        });

        popup.show();
    }

}
