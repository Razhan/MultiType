package com.example.multitypelib;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;

public class Items extends ArrayList<Object> {

  public Items() {
    super();
  }

  public Items(int initialCapacity) {
    super(initialCapacity);
  }

  public Items(@NonNull Collection<?> c) {
    super(c);
  }
}
