package com.example.multitypelib;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import java.util.List;

public abstract class AdapterDelegate<T> {

    @NonNull
    abstract protected ViewHolder onCreateViewHolder(@NonNull ViewGroup parent);

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
