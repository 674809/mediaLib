package com.egar.music.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class BaseArrAdapter<T> extends ArrayAdapter<T> {

    /**
     * Used to listener your collect operate
     */
    public interface CollectListener {
        void onClickCollectBtn(ImageView ivCollect, int pos);
    }

    public BaseArrAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }
}
