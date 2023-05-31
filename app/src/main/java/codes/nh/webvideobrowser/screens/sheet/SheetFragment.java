package codes.nh.webvideobrowser.screens.sheet;

import android.content.Context;

import androidx.fragment.app.Fragment;

public abstract class SheetFragment extends Fragment {

    private final int titleId;

    private final boolean canGoBack;

    public SheetFragment(int layoutId, int titleId, boolean canGoBack) {
        super(layoutId);
        this.titleId = titleId;
        this.canGoBack = canGoBack;
    }

    public SheetFragment(int layoutId, int titleId) {
        this(layoutId, titleId, false);
    }

    public int getTitleId() {
        return titleId;
    }

    public boolean canGoBack() {
        return canGoBack;
    }

    public Context getApplicationContext() {
        return requireContext().getApplicationContext();
    }

}
