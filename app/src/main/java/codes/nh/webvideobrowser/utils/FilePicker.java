package codes.nh.webvideobrowser.utils;

import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.function.Consumer;

public class FilePicker {

    private final AppCompatActivity activity;

    private ActivityResultLauncher<String> filePicker;

    public FilePicker(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void register(Consumer<Uri> consumer) {
        filePicker = activity.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> consumer.accept(uri)
        );
    }

    public void launch() {
        String mimeType = "*/*";
        filePicker.launch(mimeType);
    }

}
