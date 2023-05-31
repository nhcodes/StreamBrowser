package codes.nh.webvideobrowser.screens.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;

import codes.nh.webvideobrowser.screens.sheet.SheetFragment;
import codes.nh.webvideobrowser.screens.sheet.SheetRequest;
import codes.nh.webvideobrowser.utils.AppUtils;

public class MainViewModel extends AndroidViewModel {

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppUtils.log("init MainViewModel");
    }

    //sheet

    private final ArrayList<SheetRequest> sheetStack = new ArrayList<>();

    private final MutableLiveData<SheetRequest> currentSheet = new MutableLiveData<>();

    public MutableLiveData<SheetRequest> getCurrentSheet() {
        return currentSheet;
    }

    public boolean isSheetOpen() {
        return currentSheet.getValue() != null;
    }

    public void openSheet(SheetRequest sheet) {
        sheetStack.add(0, sheet);
        currentSheet.setValue(sheet);
    }

    public void goBackToPreviousSheet() {
        if (sheetStack.size() < 2) {
            closeSheet();
        } else {
            sheetStack.remove(0);
            SheetRequest lastSheet = sheetStack.get(0);
            openSheet(lastSheet);
        }
    }

    public void closeSheet() {
        currentSheet.setValue(null);
        sheetStack.clear();
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
