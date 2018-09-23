package com.example.ran.multitype.adapter;

import android.support.annotation.NonNull;

import com.example.multitypeannotations.Delegate;
import com.example.multitypeannotations.DelegateLayout;
import com.example.multitypeannotations.Type;
import com.example.multitypelib.BaseDelegationAdapter;
import com.example.multitypelib.ViewHolder;
import com.example.multitypelib.BaseSubListDelegate;
import com.example.ran.multitype.R;
import com.example.ran.multitype.model.Animal;
import com.example.ran.multitype.model.Dog;
import com.example.ran.multitype.model.Husky;

import java.util.List;

@Delegate(DETAIL = {@Type(CLASS = Dog.class, SUBTYPE = 0),
                    @Type(CLASS = Husky.class)})
@DelegateLayout(LAYOUT = R.layout.item_dog)
public class DogDelegate extends BaseSubListDelegate<Dog, Animal> {

    public DogDelegate(BaseDelegationAdapter adapter, int layoutId) {
        super(adapter, layoutId);
    }

    @Override
    public void onBindViewHolder(@NonNull Dog item, @NonNull ViewHolder holder, @NonNull List<Object> payloads) {

    }

}
