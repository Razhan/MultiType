package com.example.multitypelib.listadapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.example.multitypelib.AbsDelegationAdapter;
import com.example.multitypelib.AdapterDelegate;
import com.example.multitypelib.ViewHolder;

import java.util.List;

public abstract class AbsSubListDelegate<I extends T, T> extends AbsListDelegate<T> {

    public AbsSubListDelegate(AbsDelegationAdapter adapter, int layoutId) {
        super(adapter, layoutId);
    }

    @SuppressWarnings("unchecked")
    @Override protected final void onBindViewHolder(@NonNull List<T> items, int position, @NonNull ViewHolder holder,
                                                    @NonNull List<Object> payloads) {
        onBindViewHolder((I) items.get(position), holder, payloads);
    }

    public abstract void onBindViewHolder(@NonNull I item, @NonNull ViewHolder holder, @NonNull List<Object> payloads);

}
