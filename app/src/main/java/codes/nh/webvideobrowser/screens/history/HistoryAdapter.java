package codes.nh.webvideobrowser.screens.history;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.screens.stream.Stream;
import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.RecyclerAdapter;
import codes.nh.webvideobrowser.utils.UrlUtils;

public class HistoryAdapter extends RecyclerAdapter<Stream> {

    @Override
    public int getLayoutId() {
        return R.layout.card_history;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(View view) {
        return new Holder(view);
    }

    @Override
    public void onCreateView(RecyclerView.ViewHolder viewHolder, Stream stream) {
        Holder holder = (Holder) viewHolder;
        Context context = holder.itemView.getContext();

        holder.titleText.setText(stream.getTitle());
        holder.titleText.setSelected(true);

        String source = UrlUtils.getHostFromURL(stream.getSourceUrl());
        holder.sourceText.setText(context.getString(R.string.card_history_source, source));

        String host = UrlUtils.getHostFromURL(stream.getStreamUrl());
        holder.hostText.setText(context.getString(R.string.card_history_host, host));

        String file = UrlUtils.getFileNameFromUrl(stream.getStreamUrl());
        holder.fileText.setText(context.getString(R.string.card_history_file, file));

        String time = "LIVE";
        if (stream.getStartTime() != -1) {
            time = AppUtils.millisToMinutesSeconds(stream.getStartTime());
        }
        holder.timeText.setText(context.getString(R.string.card_history_time, time));
    }

    private static class Holder extends RecyclerView.ViewHolder {

        private final TextView titleText, sourceText, hostText, fileText, timeText;

        public Holder(View view) {
            super(view);
            this.titleText = view.findViewById(R.id.card_history_text_title);
            this.sourceText = view.findViewById(R.id.card_history_text_source);
            this.hostText = view.findViewById(R.id.card_history_text_host);
            this.fileText = view.findViewById(R.id.card_history_text_file);
            this.timeText = view.findViewById(R.id.card_history_text_time);
        }
    }
}
