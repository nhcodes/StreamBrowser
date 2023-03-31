package codes.nh.webvideobrowser.fragments.sheet;

import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import codes.nh.webvideobrowser.HomeActivity;
import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.fragments.stream.StreamsFragment;
import codes.nh.webvideobrowser.utils.AppUtils;

public class SheetManager {

    private final HomeActivity activity;

    private final ImageButton backButton;

    private final TextView titleText;

    private final ImageButton closeButton;

    private final BottomSheetBehavior<LinearLayout> behavior;

    public SheetManager(HomeActivity activity) {
        this.activity = activity;

        LinearLayout sheetLayout = activity.findViewById(R.id.fragment_sheet_layout);
        this.behavior = BottomSheetBehavior.from(sheetLayout);
        this.backButton = sheetLayout.findViewById(R.id.fragment_sheet_button_back);
        this.titleText = sheetLayout.findViewById(R.id.fragment_sheet_text_title);
        this.closeButton = sheetLayout.findViewById(R.id.fragment_sheet_button_close);
        init();
    }

    public void init() {
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    activity.clearNavigationSelection();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

    }

    public void open(SheetRequest sheetRequest) {
        //close();

        if (sheetRequest.getFragmentClass() == StreamsFragment.class) { //todo
            activity.clearNavigationSelection();
        }

        SheetFragment fragment = null;
        try {
            fragment = sheetRequest.getFragmentClass().newInstance();
        } catch (Exception e) {
            AppUtils.log("SheetManager.open()", e);
        }

        titleText.setText(fragment.getTitleId());

        Runnable backButtonListener = fragment.getBackButtonClickListener();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backButtonListener.run();
            }
        });

        backButton.setVisibility(fragment.isOverlay() ? View.VISIBLE : View.INVISIBLE);

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

        activity.clearNavigationSelection();
    }

}
