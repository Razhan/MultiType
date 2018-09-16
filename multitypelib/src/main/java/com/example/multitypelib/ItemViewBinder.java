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
//import android.support.v7.widget.RecyclerView.LayoutManager;
//import android.support.v7.widget.RecyclerView.ViewHolder;
//import android.view.LayoutInflater;
//import android.view.ViewGroup;
//
//import java.util.List;
//
//public abstract class ItemViewBinder<T, VH extends ViewHolder> {
//
//  protected MultiTypeAdapter adapter;
//
//
//  protected abstract @NonNull VH onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent);
//
//  protected abstract void onBindViewHolder(@NonNull VH holder, @NonNull T item);
//
//  protected void onBindViewHolder(@NonNull VH holder, @NonNull T item, @NonNull List<Object> payloads) {
//    onBindViewHolder(holder, item);
//  }
//
//
//  /**
//   * Get the adapter position of current item,
//   * the internal position equals to {@link ViewHolder#getAdapterPosition()}.
//   * <p><b>NOTE</b>: Below v2.3.5 we may provide getPosition() method to get the position,
//   * It exists BUG, and sometimes can not get the correct position,
//   * it is recommended to immediately stop using it and use the new
//   * {@code getPosition(ViewHolder)} instead.</p>
//   *
//   * @param holder The ViewHolder to call holder.getAdapterPosition().
//   * @return The adapter position.
//   * @since v2.3.5. If below v2.3.5, use {@link ViewHolder#getAdapterPosition()} instead.
//   */
//  protected final int getPosition(@NonNull final ViewHolder holder) {
//    return holder.getAdapterPosition();
//  }
//
//
//  /**
//   * Get the {@link MultiTypeAdapter} for sending notifications or getting item count, etc.
//   * <p>
//   * Note that if you need to change the item's parent items, you could call this method
//   * to get the {@link MultiTypeAdapter}, and call {@link MultiTypeAdapter#getItems()} to get
//   * a list that can not be added any new item, so that you should copy the items and just use
//   * {@link MultiTypeAdapter#setItems(List)} to replace the original items list and update the
//   * views.
//   * </p>
//   *
//   * @return The MultiTypeAdapter this item is currently associated with.
//   * @since v2.3.4
//   */
//  protected final @NonNull
//  MultiTypeAdapter getAdapter() {
//    if (adapter == null) {
//      throw new IllegalStateException("ItemViewBinder " + this + " not attached to MultiTypeAdapter. " +
//          "You should not call the method before registering the binder.");
//    }
//    return adapter;
//  }
//
//
//  /**
//   * Return the stable ID for the <code>item</code>. If {@link RecyclerView.Adapter#hasStableIds()}
//   * would return false this method should return {@link RecyclerView#NO_ID}. The default
//   * implementation of this method returns {@link RecyclerView#NO_ID}.
//   *
//   * @param item The item within the MultiTypeAdapter's items data set to query
//   * @return the stable ID of the item
//   * @see RecyclerView.Adapter#setHasStableIds(boolean)
//   * @since v3.2.0
//   */
//  protected long getItemId(@NonNull T item) {
//    return RecyclerView.NO_ID;
//  }
//
//
//  /**
//   * Called when a view created by this {@link ItemViewBinder} has been recycled.
//   *
//   * <p>A view is recycled when a {@link LayoutManager} decides that it no longer
//   * needs to be attached to its parent {@link RecyclerView}. This can be because it has
//   * fallen out of visibility or a set of cached views represented by views still
//   * attached to the parent RecyclerView. If an item view has large or expensive data
//   * bound to it such as large bitmaps, this may be a good place to release those
//   * resources.</p>
//   * <p>
//   * RecyclerView calls this method right before clearing ViewHolder's internal data and
//   * sending it to RecycledViewPool.
//   *
//   * @param holder The ViewHolder for the view being recycled
//   * @since v3.1.0
//   */
//  protected void onViewRecycled(@NonNull VH holder) {}
//
//
//  /**
//   * Called by the RecyclerView if a ViewHolder created by this Adapter cannot be recycled
//   * due to its transient state. Upon receiving this callback, Adapter can clear the
//   * animation(s) that effect the View's transient state and return <code>true</code> so that
//   * the View can be recycled. Keep in mind that the View in question is already removed from
//   * the RecyclerView.
//   * <p>
//   * In some cases, it is acceptable to recycle a View although it has transient state. Most
//   * of the time, this is a case where the transient state will be cleared in
//   * {@link #onBindViewHolder(ViewHolder, Object)} call when View is rebound to a new item.
//   * For this reason, RecyclerView leaves the decision to the Adapter and uses the return
//   * value of this method to decide whether the View should be recycled or not.
//   * <p>
//   * Note that when all animations are created by {@link RecyclerView.ItemAnimator}, you
//   * should never receive this callback because RecyclerView keeps those Views as children
//   * until their animations are complete. This callback is useful when children of the item
//   * views create animations which may not be easy to implement using an {@link
//   * RecyclerView.ItemAnimator}.
//   * <p>
//   * You should <em>never</em> fix this issue by calling
//   * <code>holder.itemView.setHasTransientState(false);</code> unless you've previously called
//   * <code>holder.itemView.setHasTransientState(true);</code>. Each
//   * <code>View.setHasTransientState(true)</code> call must be matched by a
//   * <code>View.setHasTransientState(false)</code> call, otherwise, the state of the View
//   * may become inconsistent. You should always prefer to end or cancel animations that are
//   * triggering the transient state instead of handling it manually.
//   *
//   * @param holder The ViewHolder containing the View that could not be recycled due to its
//   * transient state.
//   * @return True if the View should be recycled, false otherwise. Note that if this method
//   * returns <code>true</code>, RecyclerView <em>will ignore</em> the transient state of
//   * the View and recycle it regardless. If this method returns <code>false</code>,
//   * RecyclerView will check the View's transient state again before giving a final decision.
//   * Default implementation returns false.
//   * @since v3.1.0
//   */
//  protected boolean onFailedToRecycleView(@NonNull VH holder) {
//    return false;
//  }
//
//
//  /**
//   * Called when a view created by this {@link ItemViewBinder} has been attached to a window.
//   *
//   * <p>This can be used as a reasonable signal that the view is about to be seen
//   * by the user. If the {@link ItemViewBinder} previously freed any resources in
//   * {@link #onViewDetachedFromWindow(RecyclerView.ViewHolder) onViewDetachedFromWindow}
//   * those resources should be restored here.</p>
//   *
//   * @param holder Holder of the view being attached
//   * @since v3.1.0
//   */
//  protected void onViewAttachedToWindow(@NonNull VH holder) {}
//
//
//  /**
//   * Called when a view created by this {@link ItemViewBinder} has been detached from its
//   * window.
//   *
//   * <p>Becoming detached from the window is not necessarily a permanent condition;
//   * the consumer of an Adapter's views may choose to cache views offscreen while they
//   * are not visible, attaching and detaching them as appropriate.</p>
//   *
//   * @param holder Holder of the view being detached
//   * @since v3.1.0
//   */
//  protected void onViewDetachedFromWindow(@NonNull VH holder) {}
//}
