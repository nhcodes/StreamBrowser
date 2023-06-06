package codes.nh.streambrowser.screens.cast;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class CastViewModel extends AndroidViewModel {

    private final CastManager castManager;

    public CastViewModel(@NonNull Application application) {
        super(application);

        castManager = new CastManager(application);
    }

    public CastManager getCastManager() {
        return castManager;
    }

}
