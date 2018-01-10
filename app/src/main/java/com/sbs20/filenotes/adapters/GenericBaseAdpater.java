package com.sbs20.filenotes.adapters;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class GenericBaseAdpater<T> extends BaseAdapter {

    private List<T> items = Collections.emptyList();

    protected final Context context;

    // the context is needed to inflate views in getView()
    public GenericBaseAdpater(Context context) {
        this.context = context;
    }

    public void updateItems(List<T> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void updateItems(T[] items) {
        this.items = new ArrayList<T>();
        for (int index = 0; index < items.length; index++) {
            this.items.add(items[index]);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    // getItem(int) in Adapter returns Object but we can override it
    @Override
    public T getItem(int position) {
        return this.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
