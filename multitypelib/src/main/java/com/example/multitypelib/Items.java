package com.example.multitypelib;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;

public class Items<T> extends ArrayList<T> {

  public Items() {
    super();
  }

  public Items(int initialCapacity) {
    super(initialCapacity);
  }

  public Items(@NonNull Collection<T> c) {
    super(c);
  }
}
