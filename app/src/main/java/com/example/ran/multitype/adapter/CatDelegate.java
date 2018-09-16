package com.example.ran.multitype.adapter;

import android.support.annotation.NonNull;

import com.example.multitypeannotations.Delegate;
import com.example.multitypeannotations.DelegateLayout;
import com.example.multitypeannotations.Type;
import com.example.multitypelib.AbsDelegationAdapter;
import com.example.multitypelib.ViewHolder;
import com.example.multitypelib.listadapter.AbsSubListDelegate;
import com.example.ran.multitype.R;
import com.example.ran.multitype.model.Animal;
import com.example.ran.multitype.model.Cat;

import java.util.List;

@Delegate(DETAIL = @Type(CLASS = Cat.class))
@DelegateLayout(LAYOUT = R.layout.item_cat)
public class CatDelegate extends AbsSubListDelegate<Cat, Animal> {

    public CatDelegate(AbsDelegationAdapter adapter, int layoutId) {
        super(adapter, layoutId);
    }

    @Override
    public void onBindViewHolder(@NonNull Cat item, @NonNull ViewHolder holder, @NonNull List<Object> payloads) {

    }

}
