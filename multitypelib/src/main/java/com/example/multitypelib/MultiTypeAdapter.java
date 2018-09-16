///*
// * Copyright 2016 drakeet. https://github.com/drakeet
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.example.multitypelib;
//
//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.RecyclerView.ViewHolder;
//import android.view.ViewGroup;
//
//import java.util.Collections;
//import java.util.List;
//
//import static com.example.multitypelib.Preconditions.checkNotNull;
//
//public class MultiTypeAdapter extends RecyclerView.Adapter<ViewHolder> {
//
//  private @NonNull List<?> items = Collections.emptyList();
//
//  public MultiTypeAdapter() {
//    this(Collections.emptyList());
//  }
//
//  public MultiTypeAdapter(@NonNull List<?> items) {
//    checkNotNull(items);
//    this.items = items;
//  }
//
//  public void setItems(@NonNull List<?> items) {
//    this.items = items;
//  }
//
//
//  public @NonNull List<?> getItems() {
//    return items;
//  }
//
//  @Override
//  public final int getItemViewType(int position) {
//    Object item = items.get(position);
//    return indexInTypesOf(position, item);
//  }
//
//  @Override
//  public final ViewHolder onCreateViewHolder(ViewGroup parent, int indexViewType) {
////    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
////    ItemViewBinder<?, ?> binder = typePool.getItemViewBinder(indexViewType);
////    return binder.onCreateViewHolder(inflater, parent);
//  }
//
//  @Override
//  public final void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//    onBindViewHolder(holder, position, Collections.emptyList());
//  }
//
//
//  @Override
//  @SuppressWarnings("unchecked")
//  public final void onBindViewHolder(ViewHolder holder, int position, @NonNull List<Object> payloads) {
////    Object item = items.get(position);
////    ItemViewBinder binder = typePool.getItemViewBinder(holder.getItemViewType());
////    binder.onBindViewHolder(holder, item, payloads);
//  }
//
//
//  @Override
//  public final int getItemCount() {
//    return items.size();
//  }
//
////  @Override
////  @SuppressWarnings("unchecked")
////  public final void onViewRecycled(@NonNull ViewHolder holder) {
////    getRawBinderByViewHolder(holder).onViewRecycled(holder);
////  }
////
////  @Override
////  @SuppressWarnings("unchecked")
////  public final boolean onFailedToRecycleView(@NonNull ViewHolder holder) {
////    return getRawBinderByViewHolder(holder).onFailedToRecycleView(holder);
////  }
////
////  @Override
////  @SuppressWarnings("unchecked")
////  public final void onViewAttachedToWindow(@NonNull ViewHolder holder) {
////    getRawBinderByViewHolder(holder).onViewAttachedToWindow(holder);
////  }
////
////  @Override
////  @SuppressWarnings("unchecked")
////  public final void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
////    getRawBinderByViewHolder(holder).onViewDetachedFromWindow(holder);
////  }
//
//}
