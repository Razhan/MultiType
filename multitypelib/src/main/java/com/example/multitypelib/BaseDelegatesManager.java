package com.example.multitypelib;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

public abstract class BaseDelegatesManager<T> {

    protected static final int FALLBACK_DELEGATE_VIEW_TYPE = Integer.MAX_VALUE - 1;
    private static final List<Object> PAYLOADS_EMPTY_LIST = Collections.emptyList();

    private BaseAdapterDelegate<T> fallbackDelegate;

    public abstract int getItemViewType(@NonNull List<T> items, int position);

    @Nullable
    public abstract BaseAdapterDelegate<T> getDelegateForViewType(int viewType);

    public void setAdapter(BaseDelegationAdapter adapter) {
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BaseAdapterDelegate<T> delegate = getDelegateForViewType(viewType);
        if (delegate == null) {
            throw new NullPointerException("No Delegate added for ViewType " + viewType);
        }

        ViewHolder vh = delegate.onCreateViewHolder(parent);
        if (vh == null) {
            throw new NullPointerException("ViewHolder returned from Delegate "
                    + delegate
                    + " for ViewType ="
                    + viewType
                    + " is null!");
        }
        return vh;
    }

    public void onBindViewHolder(@NonNull List<T> items, int position, @NonNull ViewHolder holder, List<Object> payloads) {
        BaseAdapterDelegate<T> delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for item at position = "
                    + position
                    + " for viewType = "
                    + holder.getItemViewType());
        }
        delegate.onBindViewHolder(items, position, holder, payloads != null ? payloads : PAYLOADS_EMPTY_LIST);
    }

    public void onBindViewHolder(@NonNull List<T> items, int position, @NonNull ViewHolder holder) {
        onBindViewHolder(items, position, holder, PAYLOADS_EMPTY_LIST);
    }

    public void onViewRecycled(@NonNull ViewHolder holder) {
        BaseAdapterDelegate<T> delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for "
                    + holder
                    + " for item at position = "
                    + holder.getAdapterPosition()
                    + " for viewType = "
                    + holder.getItemViewType());
        }
        delegate.onViewRecycled(holder);
    }

    public boolean onFailedToRecycleView(@NonNull ViewHolder holder) {
        BaseAdapterDelegate<T> delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for "
                    + holder
                    + " for item at position = "
                    + holder.getAdapterPosition()
                    + " for viewType = "
                    + holder.getItemViewType());
        }
        return delegate.onFailedToRecycleView(holder);
    }

    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        BaseAdapterDelegate<T> delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for "
                    + holder
                    + " for item at position = "
                    + holder.getAdapterPosition()
                    + " for viewType = "
                    + holder.getItemViewType());
        }
        delegate.onViewAttachedToWindow(holder);
    }

    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        BaseAdapterDelegate<T> delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate == null) {
            throw new NullPointerException("No delegate found for "
                    + holder
                    + " for item at position = "
                    + holder.getAdapterPosition()
                    + " for viewType = "
                    + holder.getItemViewType());
        }
        delegate.onViewDetachedFromWindow(holder);
    }

    public BaseDelegatesManager<T> setFallbackDelegate(@Nullable BaseAdapterDelegate<T> fallbackDelegate) {
        this.fallbackDelegate = fallbackDelegate;
        return this;
    }

    @Nullable
    public BaseAdapterDelegate<T> getFallbackDelegate() {
        return fallbackDelegate;
    }

}
