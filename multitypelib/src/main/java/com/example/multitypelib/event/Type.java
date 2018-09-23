package com.example.multitypelib.event;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.example.multitypelib.event.Type.TYPE_ITEM_CHANGED;
import static com.example.multitypelib.event.Type.TYPE_ITEM_INSERTED;
import static com.example.multitypelib.event.Type.TYPE_ITEM_JUST_NOTIFY;
import static com.example.multitypelib.event.Type.TYPE_ITEM_MOVED;
import static com.example.multitypelib.event.Type.TYPE_ITEM_RANGE_CHANGED;
import static com.example.multitypelib.event.Type.TYPE_ITEM_RANGE_INSERTED;
import static com.example.multitypelib.event.Type.TYPE_ITEM_RANGE_REMOVED;
import static com.example.multitypelib.event.Type.TYPE_ITEM_REMOVED;

@Retention(RetentionPolicy.SOURCE)
@IntDef({
        TYPE_ITEM_JUST_NOTIFY,
        TYPE_ITEM_INSERTED,
        TYPE_ITEM_RANGE_INSERTED,
        TYPE_ITEM_CHANGED,
        TYPE_ITEM_RANGE_CHANGED,
        TYPE_ITEM_REMOVED,
        TYPE_ITEM_RANGE_REMOVED,
        TYPE_ITEM_MOVED
        })
public @interface Type {
    int TYPE_ITEM_JUST_NOTIFY = 0;
    int TYPE_ITEM_INSERTED = 1;
    int TYPE_ITEM_RANGE_INSERTED = 2;
    int TYPE_ITEM_CHANGED = 3;
    int TYPE_ITEM_RANGE_CHANGED = 4;
    int TYPE_ITEM_REMOVED = 5;
    int TYPE_ITEM_RANGE_REMOVED = 6;
    int TYPE_ITEM_MOVED = 7;
}
