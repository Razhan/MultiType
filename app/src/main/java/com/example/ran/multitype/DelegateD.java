package com.example.ran.multitype;

import com.example.multitypeannotations.AdapterDelegate;
import com.example.multitypeannotations.DelegateLayout;
import com.example.multitypeannotations.Type;
import com.example.multitypelib.AbsDelegationAdapter;
import com.example.multitypelib.Delegate;

@AdapterDelegate(DETAIL = @Type(CLASS = TestC.class, SUBTYPE = 8))
@DelegateLayout(LAYOUT = R.layout.activity_main)
public class DelegateD extends Delegate {

    public DelegateD(AbsDelegationAdapter adapter, int layoutId) {
        super(adapter, layoutId);
    }
}