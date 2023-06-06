package codes.nh.streambrowser.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class RecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public abstract int getLayoutId();

    public abstract RecyclerView.ViewHolder getViewHolder(View view);

    public abstract void onCreateView(RecyclerView.ViewHolder viewHolder, T element);

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(getLayoutId(), parent, false);
        return getViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        T element = list.get(position);

        View rootView = viewHolder.itemView;

        rootView.setOnClickListener(view -> listener.onClick(element));

        rootView.setOnLongClickListener(view -> {
            listener.onLongClick(element);
            return true;
        });

        onCreateView(viewHolder, element);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //list

    private List<T> list = new ArrayList<>();

    public void set(List<T> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    /*
    public void add(T element) {
        T duplicate = getDuplicate(element);
        if (duplicate != null) {
            remove(duplicate);
        }
        list.add(element);
        notifyItemInserted(list.size() - 1);
    }

    public void remove(T element) {
        int index = list.indexOf(element);
        if (index == -1) {
            return;
        }

        list.remove(index);
        notifyItemRemoved(index);
    }

    public T getDuplicate(T element) {
        for (T elements : list) {
            if (elements.equals(element)) {
                return elements;
            }
        }
        return null;
    }*/

    //listener

    private Listener<T> listener;

    public void setListener(Listener<T> listener) {
        this.listener = listener;
    }

    public interface Listener<T> {
        void onClick(T element);

        void onLongClick(T element);
    }
}
