package codes.nh.streambrowser.screens.bookmark;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import codes.nh.streambrowser.R;
import codes.nh.streambrowser.utils.ImageUtils;
import codes.nh.streambrowser.utils.RecyclerAdapter;

public class BookmarksAdapter extends RecyclerAdapter<Bookmark> {

    @Override
    public int getLayoutId() {
        return R.layout.card_bookmark;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(View view) {
        return new Holder(view);
    }

    @Override
    public void onCreateView(RecyclerView.ViewHolder viewHolder, Bookmark bookmark) {
        Holder holder = (Holder) viewHolder;

        holder.titleText.setText(bookmark.getTitle());
        holder.titleText.setSelected(true);

        holder.urlText.setText(bookmark.getUrl());

        holder.faviconImage.setImageBitmap(ImageUtils.bitmapFromBytes(bookmark.getFavicon()));
    }

    private static class Holder extends RecyclerView.ViewHolder {

        private final ImageView faviconImage;

        private final TextView titleText, urlText;

        public Holder(View view) {
            super(view);
            this.faviconImage = view.findViewById(R.id.card_bookmark_image_favicon);
            this.titleText = view.findViewById(R.id.card_bookmark_text_title);
            this.urlText = view.findViewById(R.id.card_bookmark_text_url);
        }
    }
}
