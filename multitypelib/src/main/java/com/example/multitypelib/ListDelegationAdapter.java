package com.example.multitypelib;

import android.support.annotation.NonNull;

import java.util.List;

public class ListDelegationAdapter<T extends List<?>> extends AbsDelegationAdapter<T> {

    public ListDelegationAdapter(@NonNull AbsAdapterDelegatesManager<T> delegatesManager) {
        super(delegatesManager);
    }

    @Override public int getItemCount() {
        return getItems() == null ? 0 : getItems().size();
    }
}
