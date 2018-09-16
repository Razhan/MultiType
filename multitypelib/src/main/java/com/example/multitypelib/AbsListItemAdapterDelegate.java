package com.example.multitypelib;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.List;

public abstract class AbsListItemAdapterDelegate<I extends T, T, VH extends RecyclerView.ViewHolder>
        extends AdapterDelegate<List<T>> {

    @Override protected final void onBindViewHolder(@NonNull List<T> items, int position, @NonNull ViewHolder holder, @NonNull List<Object> payloads) {
        onBindViewHolder((I) items.get(position), (VH) holder, payloads);
    }

    public abstract void onBindViewHolder(@NonNull I item, @NonNull VH holder, @NonNull List<Object> payloads);
}
