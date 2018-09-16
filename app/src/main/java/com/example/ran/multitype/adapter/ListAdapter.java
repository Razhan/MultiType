package com.example.ran.multitype.adapter;

import android.support.annotation.NonNull;

import com.example.multitypelib.listadapter.ListDelegationAdapter;
import com.example.ran.multitype.model.Animal;

public class ListAdapter extends ListDelegationAdapter<Animal> {

    public ListAdapter() {
        this(new AnimalDelegatesManager());
    }

    public ListAdapter(@NonNull AnimalDelegatesManager delegatesManager) {
        super(delegatesManager);
    }

}
