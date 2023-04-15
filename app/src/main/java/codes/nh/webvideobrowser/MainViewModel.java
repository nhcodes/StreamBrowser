package codes.nh.webvideobrowser;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import codes.nh.webvideobrowser.fragments.sheet.SheetFragment;
import codes.nh.webvideobrowser.fragments.sheet.SheetRequest;
import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.SnackbarRequest;

public class MainViewModel extends AndroidViewModel {

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppUtils.log("init MainViewModel");
    }

    //sheet

    private final MutableLiveData<SheetRequest> currentSheet = new MutableLiveData<>();

    public MutableLiveData<SheetRequest> getCurrentSheet() {
        return currentSheet;
    }

    public boolean isSheetOpen() {
        return currentSheet.getValue() != null;
    }

    public void openSheet(SheetRequest sheet) {
        currentSheet.setValue(sheet);
    }

    public void closeSheet() {
        currentSheet.setValue(null);
    }

    public void closeSheet(Class<? extends SheetFragment> sheetFragmentClass) {
        SheetRequest sheet = currentSheet.getValue();
        if (sheet == null) return;
        if (!sheetFragmentClass.equals(sheet.getFragmentClass())) return;
        closeSheet();
    }

    //snackbar

    private final MutableLiveData<SnackbarRequest> snackbarMessage = new MutableLiveData<>();

    public void showSnackbar(SnackbarRequest message) {
        snackbarMessage.setValue(message);
    }

    public MutableLiveData<SnackbarRequest> getSnackbarMessage() {
        return snackbarMessage;
    }
}
