package codes.nh.webvideobrowser.fragments.stream;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.utils.ImageUtils;
import codes.nh.webvideobrowser.utils.RecyclerAdapter;
import codes.nh.webvideobrowser.utils.UrlUtils;

public class StreamsAdapter extends RecyclerAdapter<Stream> {

    @Override
    public int getLayoutId() {
        return R.layout.card_stream;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(View view) {
        return new Holder(view);
    }

    @Override
    public void onCreateView(RecyclerView.ViewHolder viewHolder, Stream stream) {
        Holder holder = (Holder) viewHolder;

        holder.fileText.setText(UrlUtils.getFileNameFromUrl(stream.getStreamUrl()));
        holder.fileText.setSelected(true);

        holder.urlText.setText(stream.getStreamUrl());

        if (!stream.getThumbnailUrls().isEmpty()) {
            ImageUtils.setImageViewFromUrl(holder.thumbnailImage, stream.getThumbnailUrls().get(0), R.drawable.icon_video, 4);
        }
    }

    private static class Holder extends RecyclerView.ViewHolder {

        private final TextView fileText, urlText;

        private final ImageView thumbnailImage;

        public Holder(View view) {
            super(view);
            this.fileText = view.findViewById(R.id.card_stream_text_file);
            this.urlText = view.findViewById(R.id.card_stream_text_url);
            this.thumbnailImage = view.findViewById(R.id.card_stream_image_thumbnail);
        }
    }
}
