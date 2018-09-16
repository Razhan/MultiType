package com.example.ran.multitype;

import com.example.multitypeannotations.TypeMethod;

public class TestA {
    public int count;

    @TypeMethod
    int test() {
        return 0;
    }

}
