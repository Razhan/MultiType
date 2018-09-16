package com.example.ran.multitype;

import com.example.multitypeannotations.AdapterDelegate;
import com.example.multitypeannotations.DelegateLayout;
import com.example.multitypeannotations.Type;
import com.example.multitypelib.AbsDelegationAdapter;
import com.example.multitypelib.Delegate;

@AdapterDelegate(DETAIL = @Type(CLASS = TestA.class))
@DelegateLayout(LAYOUT = R.layout.activity_main)
public class DelegateA extends Delegate {

    public DelegateA(AbsDelegationAdapter adapter, int layoutId) {
        super(adapter, layoutId);
    }
}
