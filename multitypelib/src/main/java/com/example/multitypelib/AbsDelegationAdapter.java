package com.example.multitypelib;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

public abstract class AbsDelegationAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    private AbsDelegatesManager<T> delegatesManager;
    private T items;

    public AbsDelegationAdapter(@NonNull AbsDelegatesManager<T> delegatesManager) {
        if (delegatesManager == null) {
            throw new NullPointerException("AbsDelegatesManager is null");
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List  payloads) {
        delegatesManager.onBindViewHolder(items, position, holder, payloads);
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
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

    public T getItems() {
        return items;
    }

    public void setItems(T items) {
        this.items = items;
    }

}
