package com.example.multitypelib;

import android.support.annotation.NonNull;

import java.util.List;

public abstract class BaseSubListDelegate<I extends T, T> extends BaseAdapterDelegate<T> {

    public BaseSubListDelegate(BaseDelegationAdapter adapter, int layoutId) {
        super(adapter, layoutId);
    }

    @SuppressWarnings("unchecked")
    @Override protected final void onBindViewHolder(@NonNull List<T> items, int position, @NonNull ViewHolder holder,
                                                    @NonNull List<Object> payloads) {
        onBindViewHolder((I) items.get(position), holder, payloads);
    }

    public abstract void onBindViewHolder(@NonNull I item, @NonNull ViewHolder holder, @NonNull List<Object> payloads);

}
