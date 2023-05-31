package codes.nh.webvideobrowser.screens.sheet;

import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.utils.AppUtils;

public class SheetManager {

    private final AppCompatActivity activity;

    private final ImageButton backButton;

    private final TextView titleText;

    private final ImageButton closeButton;

    private final BottomSheetBehavior<LinearLayout> behavior;

    public SheetManager(AppCompatActivity activity) {
        this.activity = activity;

        LinearLayout sheetLayout = activity.findViewById(R.id.fragment_sheet_layout);
        this.behavior = BottomSheetBehavior.from(sheetLayout);
        this.backButton = sheetLayout.findViewById(R.id.fragment_sheet_button_back);
        this.titleText = sheetLayout.findViewById(R.id.fragment_sheet_text_title);
        this.closeButton = sheetLayout.findViewById(R.id.fragment_sheet_button_close);
        init();
    }

    private void init() {
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    listener.onClosed();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        backButton.setOnClickListener(view -> listener.onRequestGoBack());
        closeButton.setOnClickListener(view -> listener.onRequestClose());

    }

    public void open(SheetRequest sheetRequest) {

        listener.onOpen(sheetRequest);

        SheetFragment fragment = null;
        try {
            fragment = sheetRequest.getFragmentClass().newInstance();
        } catch (Exception e) {
            AppUtils.log("SheetManager.open()", e);
        }

        titleText.setText(fragment.getTitleId());

        backButton.setVisibility(fragment.canGoBack() ? View.VISIBLE : View.INVISIBLE);

        activity.getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_sheet_frame_content, fragment)
                .runOnCommit(() -> behavior.setState(BottomSheetBehavior.STATE_COLLAPSED))
                .commit();

    }

    public void close() {
        if (behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            return;
        }

        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        activity.getSupportFragmentManager().getFragments().forEach(fragment -> {
            if (fragment.getId() == R.id.fragment_sheet_frame_content) {
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .remove(fragment)
                        .commit();
            }
        });

        //listener.onClosed(); todo already done in onStateChange
    }

    //listener

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {

        void onOpen(SheetRequest sheetRequest);

        void onRequestGoBack();

        void onRequestClose();

        void onClosed();

    }

}
