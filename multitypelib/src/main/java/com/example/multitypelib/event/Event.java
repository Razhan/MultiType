package com.example.multitypelib.event;

import java.util.List;

public final class Event<T> {

   private int position;
   private int newPosition;
   @Type private int type;
   private List<T> dataList;
   private T data;
   private Object payload;

   private Event(Builder<T> builder) {
      position = builder.position;
      newPosition = builder.newPosition;
      type = builder.type;
      dataList = builder.dataList;
      data = builder.data;
      payload = builder.payload;
   }

   public static final class Builder<V> {
      private int position;
      private int newPosition;
      private int type;
      private List<V> dataList;
      private V data;
      private Object payload;

      public Builder() {
      }

      public Builder<V> position(int val) {
         position = val;
         return this;
      }

      public Builder<V> newPosition(int val) {
         newPosition = val;
         return this;
      }

      public Builder<V> type(@Type int val) {
         type = val;
         return this;
      }

      public Builder<V> dataList(List<V> val) {
         dataList = val;
         return this;
      }

      public Builder<V> data(V val) {
         data = val;
         return this;
      }

      public Builder<V> payload(Object val) {
         payload = val;
         return this;
      }

      public Event<V> build() {
         return new Event<>(this);
      }
   }

   public int getPosition() {
      return position;
   }

   public int getNewPosition() {
      return newPosition;
   }

   public int getType() {
      return type;
   }

   public List<T> getDataList() {
      return dataList;
   }

   public T getData() {
      return data;
   }

   public Object getPayload() {
      return payload;
   }
}
