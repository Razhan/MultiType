package com.example.multitypelib.listadapter;

import android.support.annotation.NonNull;

import com.example.multitypelib.AbsDelegationAdapter;

import java.util.List;

public class ListDelegationAdapter<T> extends AbsDelegationAdapter<List<T>> {

    public ListDelegationAdapter(@NonNull AbsListDelegatesManager<T> delegatesManager) {
        super(delegatesManager);
    }

    @Override
    public int getItemCount() {
        return getItems() == null ? 0 : getItems().size();
    }
}
