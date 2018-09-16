package com.example.multitypelib.listadapter;

import com.example.multitypelib.AbsDelegationAdapter;
import com.example.multitypelib.AdapterDelegate;

import java.util.List;

public abstract class AbsListDelegate<T> extends AdapterDelegate<List<T>> {

    public AbsListDelegate(AbsDelegationAdapter adapter, int layoutId) {
        super(adapter, layoutId);
    }
}
