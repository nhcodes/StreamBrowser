package codes.nh.webvideobrowser.fragments.history;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.fragments.stream.Stream;
import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.ImageUtils;
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

        String host = UrlUtils.getHostFromURL(stream.getStreamUrl());
        holder.hostText.setText(context.getString(R.string.card_history_host, host));

        String file = UrlUtils.getFileNameFromUrl(stream.getStreamUrl());
        holder.fileText.setText(context.getString(R.string.card_history_file, file));

        String time = "LIVE";
        if (stream.getStartTime() != -1) {
            time = AppUtils.millisToMinutesSeconds(stream.getStartTime());
        }
        holder.timeText.setText(context.getString(R.string.card_history_time, time));

        if (!stream.getThumbnailUrls().isEmpty()) {
            ImageUtils.setImageViewFromUrl(holder.thumbnailImage, stream.getThumbnailUrls().get(0), R.drawable.icon_video, 4);
        }
    }

    private static class Holder extends RecyclerView.ViewHolder {

        private final TextView titleText, hostText, fileText, timeText;

        private final ImageView thumbnailImage;

        public Holder(View view) {
            super(view);
            this.titleText = view.findViewById(R.id.card_history_text_title);
            this.hostText = view.findViewById(R.id.card_history_text_host);
            this.fileText = view.findViewById(R.id.card_history_text_file);
            this.timeText = view.findViewById(R.id.card_history_text_time);
            this.thumbnailImage = view.findViewById(R.id.card_history_image_thumbnail);
        }
    }
}
