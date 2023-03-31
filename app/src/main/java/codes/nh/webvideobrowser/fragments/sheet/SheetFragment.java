package codes.nh.webvideobrowser.fragments.sheet;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class SheetFragment extends Fragment {

    private final int titleId;

    private final boolean isOverlay;

    private final OnBackPressedCallback backPressedCallback;

    public SheetFragment(int layoutId, int titleId, boolean isOverlay) {
        super(layoutId);
        this.titleId = titleId;
        this.isOverlay = isOverlay;
        this.backPressedCallback = new OnBackPressedCallback(isOverlay) {
            @Override
            public void handleOnBackPressed() {
                getBackButtonClickListener().run();
            }
        };
    }

    public SheetFragment(int layoutId, int titleId) {
        this(layoutId, titleId, false);
    }

    public int getTitleId() {
        return titleId;
    }

    public boolean isOverlay() {
        return isOverlay;
    }

    public Context getApplicationContext() {
        return requireContext().getApplicationContext();
    }

    //back button

    public Runnable getBackButtonClickListener() {
        return () -> {

        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(backPressedCallback);
    }

    @Override
    public void onDestroy() {
        backPressedCallback.remove();
        super.onDestroy();
    }
}
