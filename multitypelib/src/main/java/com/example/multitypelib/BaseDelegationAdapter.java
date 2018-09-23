package com.example.multitypelib;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.example.multitypelib.event.Event;

import java.util.Collections;
import java.util.List;

import static com.example.multitypelib.event.Type.TYPE_ITEM_CHANGED;
import static com.example.multitypelib.event.Type.TYPE_ITEM_INSERTED;
import static com.example.multitypelib.event.Type.TYPE_ITEM_JUST_NOTIFY;
import static com.example.multitypelib.event.Type.TYPE_ITEM_MOVED;
import static com.example.multitypelib.event.Type.TYPE_ITEM_RANGE_CHANGED;
import static com.example.multitypelib.event.Type.TYPE_ITEM_RANGE_INSERTED;
import static com.example.multitypelib.event.Type.TYPE_ITEM_RANGE_REMOVED;
import static com.example.multitypelib.event.Type.TYPE_ITEM_REMOVED;

public abstract class BaseDelegationAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    private BaseDelegatesManager<T> delegatesManager;
    private List<T> items;

    public BaseDelegationAdapter(@NonNull BaseDelegatesManager<T> delegatesManager) {
        if (delegatesManager == null) {
            throw new NullPointerException("BaseDelegatesManager is null");
        }

        this.delegatesManager = delegatesManager;
        this.delegatesManager.setAdapter(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        delegatesManager.onBindViewHolder(items, position, holder, null);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        delegatesManager.onBindViewHolder(items, position, holder, payloads);
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
    }

    @Override
    public int getItemCount() {
        return getItems() == null ? 0 : getItems().size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        delegatesManager.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull ViewHolder holder) {
        return delegatesManager.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        delegatesManager.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        delegatesManager.onViewDetachedFromWindow(holder);
    }

    public void Notify(Event<T> event) {
        if (event == null) {
            return;
        }

        int pos = event.getPosition();
        Object payload = event.getPayload();
        T data =  event.getData();
        List<T> dataList = event.getDataList();

        switch (event.getType()) {
            case TYPE_ITEM_INSERTED:
                if (data == null) {
                    break;
                }
                items.add(pos, data);
                notifyItemInserted(pos);
                break;
            case TYPE_ITEM_RANGE_INSERTED:
                if (dataList == null || dataList.isEmpty()) {
                    break;
                }
                items.addAll(pos, dataList);
                notifyItemRangeInserted(pos, dataList.size());
                break;
            case TYPE_ITEM_CHANGED:
                if (data == null || pos < 0 || pos >= items.size()) {
                    break;
                }

                items.set(pos, data);
                notifyItemChanged(pos, payload);
                break;
            case TYPE_ITEM_RANGE_CHANGED:
                if (dataList == null || dataList.isEmpty()) {
                    break;
                }

                if (pos < 0 || pos + dataList.size() - 1 >= items.size()) {
                    break;
                }

                for (int i = 0; i < dataList.size(); i++) {
                    items.set(pos + i, data);
                }
                notifyItemRangeChanged(pos, dataList.size(), payload);
                break;
            case TYPE_ITEM_REMOVED:
                if (pos < 0 || pos >= items.size()) {
                    break;
                }
                items.remove(pos);
                notifyItemRemoved(pos);
                break;
            case TYPE_ITEM_RANGE_REMOVED:
                if (dataList == null) {
                    break;
                }

                if (items.removeAll(dataList)) {
                    notifyItemRangeRemoved(pos, dataList.size());
                }
                break;
            case TYPE_ITEM_MOVED:
                int newPosition = event.getNewPosition();
                if (pos < 0 || pos >= items.size() || newPosition < 0 || newPosition >= items.size()) {
                    break;
                }

                Collections.swap(items, pos, newPosition);
                notifyItemMoved(pos, event.getNewPosition());
                break;
            case TYPE_ITEM_JUST_NOTIFY:
                notifyItemChanged(pos);
                break;
            default:
                Log.e("handleEvent",  String.format("hasn't define TYPE %d", event.getType()));
                break;
        }
    }

    protected List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

}
