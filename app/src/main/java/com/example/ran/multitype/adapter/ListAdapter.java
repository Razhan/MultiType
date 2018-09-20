package com.example.ran.multitype.adapter;

import android.support.annotation.NonNull;

import com.example.multitypeannotations.DelegateAdapter;
import com.example.multitypelib.listadapter.ListDelegationAdapter;
import com.example.ran.multitype.model.Animal;
@DelegateAdapter
public class ListAdapter extends ListDelegationAdapter<Animal> {

    public ListAdapter() {
        this(new ListAdapterManager());
    }

    public ListAdapter(@NonNull ListAdapterManager delegatesManager) {
        super(delegatesManager);
    }

}
