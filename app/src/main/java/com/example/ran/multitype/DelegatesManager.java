package com.example.ran.multitype;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.multitypelib.AbsAdapterDelegatesManager;
import com.example.multitypelib.AbsDelegationAdapter;
import com.example.multitypelib.AdapterDelegate;

import java.util.List;

public class DelegatesManager extends AbsAdapterDelegatesManager<List<Animal>> {

    @Override
    public void setAdapter(AbsDelegationAdapter adapter) {
        AdapterTypeIndex.getInstance().setAdapter(adapter);
    }

    @Override
    public int getItemViewType(@NonNull List<Animal> items, int position) {
        return AdapterTypeIndex.getInstance().getItemViewType(items.get(position));
    }

    @Nullable
    @Override
    public AdapterDelegate<List<Animal>> getDelegateForViewType(int viewType) {
        return null;
    }
}
