package com.example.multitypelib;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

public abstract class AdapterDelegate<T> {

    protected AbsDelegationAdapter adapter;
    protected @LayoutRes int layoutId;

    public AdapterDelegate(AbsDelegationAdapter adapter, int layoutId) {
        this.adapter = adapter;
        this.layoutId = layoutId;
    }

    @NonNull
    protected ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        if (layoutId <= 0) {
            throw new IllegalArgumentException("Can not create ViewHolder without layoutId");
        }
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false));
    }

    protected abstract void onBindViewHolder(@NonNull T items, int position, @NonNull ViewHolder holder, @NonNull List<Object> payloads);


    protected void onViewRecycled(@NonNull ViewHolder holder) {
    }

    protected boolean onFailedToRecycleView(@NonNull ViewHolder holder) {
        return false;
    }

    protected void onViewAttachedToWindow(@NonNull ViewHolder holder) {
    }

    protected void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
    }

}
