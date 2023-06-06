package codes.nh.webvideobrowser.screens.help;

import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;

import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.screens.sheet.SheetFragment;
import codes.nh.webvideobrowser.utils.AppUtils;

public class HelpFragment extends SheetFragment {

    public HelpFragment() {
        super(R.layout.fragment_help, R.string.navigation_title_help);
        AppUtils.log("init HelpFragment");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spanned html = HtmlCompat.fromHtml(getString(R.string.help_html), HtmlCompat.FROM_HTML_MODE_LEGACY);

        TextView helpText = view.findViewById(R.id.fragment_help_text);
        helpText.setText(html);

    }
}
