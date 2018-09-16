package com.example.ran.multitype.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.multitypelib.AbsDelegationAdapter;
import com.example.multitypelib.listadapter.AbsListDelegate;
import com.example.multitypelib.listadapter.AbsSubListDelegate;
import com.example.multitypelib.listadapter.AbsListDelegatesManager;
import com.example.ran.multitype.AdapterTypeIndex;
import com.example.ran.multitype.model.Animal;

import java.util.List;

public class AnimalDelegatesManager extends AbsListDelegatesManager<Animal> {

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
    @SuppressWarnings("unchecked")
    public AbsListDelegate<Animal> getDelegateForViewType(int viewType) {
        return (AbsListDelegate<Animal>) AdapterTypeIndex.getInstance().getDelegateByViewType(viewType);
    }

}
