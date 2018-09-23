package com.example.ran.multitype.adapter;

import android.support.annotation.NonNull;

import com.example.multitypeannotations.DelegateAdapter;
import com.example.multitypelib.BaseDelegationAdapter;
import com.example.ran.multitype.model.Animal;
@DelegateAdapter
public class ListAdapter extends BaseDelegationAdapter<Animal> {

    public ListAdapter() {
        this(new ListAdapterManager());
    }

    public ListAdapter(@NonNull ListAdapterManager delegatesManager) {
        super(delegatesManager);
    }

}
