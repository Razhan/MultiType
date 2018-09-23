package com.example.multitypelib;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

public abstract class BaseAdapterDelegate<T> {

    private BaseDelegationAdapter adapter;
    @LayoutRes
    private int layoutId;

    public BaseAdapterDelegate(BaseDelegationAdapter adapter, int layoutId) {
        this.adapter = adapter;
        this.layoutId = layoutId;
    }

    @NonNull
    protected ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        if (layoutId <= 0) {
            throw new IllegalArgumentException("Can not create ViewHolder without layoutId");
        }
        ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false));
        setHolderListener(holder);

        return holder;
    }

    protected void setHolderListener(ViewHolder holder) {

    }

    protected abstract void onBindViewHolder(@NonNull List<T> items, int position, @NonNull ViewHolder holder, @NonNull List<Object> payloads);


    protected void onViewRecycled(@NonNull ViewHolder holder) {
    }

    protected boolean onFailedToRecycleView(@NonNull ViewHolder holder) {
        return false;
    }

    protected void onViewAttachedToWindow(@NonNull ViewHolder holder) {
    }

    protected void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
    }

    public BaseDelegationAdapter getAdapter() {
        return adapter;
    }
}
