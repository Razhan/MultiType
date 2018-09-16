package com.example.ran.multitype.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.multitypeannotations.AdapterDelegate;
import com.example.multitypeannotations.DelegateLayout;
import com.example.multitypeannotations.Type;
import com.example.multitypelib.AbsDelegationAdapter;
import com.example.multitypelib.ViewHolder;
import com.example.multitypelib.listadapter.AbsSubListDelegate;
import com.example.ran.multitype.R;
import com.example.ran.multitype.model.Animal;
import com.example.ran.multitype.model.Cat;
import com.example.ran.multitype.model.Dog;

import java.util.List;

@AdapterDelegate(DETAIL = @Type(CLASS = Dog.class, SUBTYPE = 0))
@DelegateLayout(LAYOUT = R.layout.item_dog)
public class DogDelegate extends AbsSubListDelegate<Dog, Animal> {

    public DogDelegate(AbsDelegationAdapter adapter, int layoutId) {
        super(adapter, layoutId);
    }

    @Override
    public void onBindViewHolder(@NonNull Dog item, @NonNull ViewHolder holder, @NonNull List<Object> payloads) {

    }

}
