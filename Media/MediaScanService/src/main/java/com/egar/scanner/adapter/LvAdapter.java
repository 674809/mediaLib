package com.egar.scanner.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.egar.scanner.R;

import java.util.List;

import juns.lib.media.bean.StorageDevice;

public class LvAdapter extends ArrayAdapter {
    private Context mContext;
    private List mList;

    public LvAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        mContext = context;
    }

    public void refreshData(List listStr) {
        synchronized (this) {
            mList = listStr;
            notifyDataSetChanged();
        }
    }

    public void setListStr(List listStr) {
        mList = listStr;
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        if (mList == null || mList.size() == 0) {
            return null;
        }
        return mList.get(position);
    }

    @Override
    public int getCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_main_item, parent, false);
            holder = new ViewHolder();
            holder.tvItem = (TextView) convertView.findViewById(R.id.tv_data_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Object objItem = getItem(position);
        if (objItem instanceof StorageDevice) {
            StorageDevice storage = (StorageDevice) objItem;
            holder.tvItem.setText(position + " - " + storage.getStorageId() + " - " + storage.isMounted() + " - " + storage.getRoot());
        } else {
            holder.tvItem.setText(position + " - " + objItem);
        }

        return convertView;
    }

    private final class ViewHolder {
        private TextView tvItem;
    }
}
