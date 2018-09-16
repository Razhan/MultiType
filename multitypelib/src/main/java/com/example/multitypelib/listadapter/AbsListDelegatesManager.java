package com.example.multitypelib.listadapter;

import android.support.annotation.Nullable;

import com.example.multitypelib.AbsDelegatesManager;

import java.util.List;

public abstract class AbsListDelegatesManager<T> extends AbsDelegatesManager<List<T>> {

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public AbsListDelegate<T> getDelegateForViewType(int viewType) {
        return null;
    }

}
