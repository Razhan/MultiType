package com.example.ran.multitype.model;

import com.example.multitypeannotations.TypeMethod;

public class Dog extends Animal {

  private int type;

  public Dog(String name) {
    super(name);
    this.type = 0;
  }

  public Dog(String name, int type) {
    super(name);
    this.type = type;
  }

  @TypeMethod
  public int getSubType() {
    return type;
  }




}
