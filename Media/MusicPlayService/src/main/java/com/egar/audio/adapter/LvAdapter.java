package com.egar.audio.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.egar.audio.R;

import java.util.List;

import juns.lib.android.utils.Logs;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.bean.ProAudioSheet;
import juns.lib.media.bean.ProAudioSheetMapInfo;
import juns.lib.media.bean.StorageDevice;

public class LvAdapter extends ArrayAdapter {
    //TAG
    private static final String TAG = "LvAdapter";

    //
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List mListData;
    private String mPlayingMediaUrl;

    public LvAdapter(Context context, int resource) {
        super(context, resource);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void refreshData(List list) {
        synchronized (this) {
            mListData = list;
            notifyDataSetChanged();
        }
    }

    public void refreshPlaying(String playingMediaUrl) {
        synchronized (this) {
            mPlayingMediaUrl = playingMediaUrl;
            notifyDataSetChanged();
        }
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.v_rcv, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            java.lang.Object objItem = mListData.get(position);
            if (objItem instanceof StorageDevice) {
                StorageDevice storage = (StorageDevice) objItem;
                holder.mmTvContent.setText((position + 1)
                        + " - " + storage.getStorageId()
                        + " - " + storage.isMounted()
                        + " - " + storage.getRoot());

            } else if (objItem instanceof ProAudio) {
                ProAudio media = (ProAudio) objItem;
                holder.mmTvContent.setText(media.getId()
                        + " - " + media.getTitle()
                        + "\n" + media.getMediaUrl());
                if (TextUtils.equals(mPlayingMediaUrl, media.getMediaUrl())) {
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.turquoise, null));
                } else {
                    convertView.setBackgroundColor(mContext.getResources().getColor(android.R.color.white, null));
                }

            } else if (objItem instanceof ProAudioSheet) {
                ProAudioSheet mediaSheet = (ProAudioSheet) objItem;
                holder.mmTvContent.setText(mediaSheet.getId()
                        + " - " + mediaSheet.getTitle()
                        + " - " + mediaSheet.getUpdateTime());

            } else if (objItem instanceof ProAudioSheetMapInfo) {
                ProAudioSheetMapInfo mediaSheet = (ProAudioSheetMapInfo) objItem;
                holder.mmTvContent.setText(mediaSheet.getId()
                        + " - " + mediaSheet.getSheetId()
                        + "\n <-->" + mediaSheet.getMediaUrl());
            }

            if (!(objItem instanceof ProAudio)) {
                convertView.setBackgroundColor(mContext.getResources().getColor(android.R.color.white, null));
            }
        } catch (Exception e) {
            Logs.i(TAG, "");
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mListData == null ? 0 : mListData.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        if (mListData == null) {
            return null;
        }
        return mListData.get(position);
    }

    private class ViewHolder {
        TextView mmTvContent;

        ViewHolder(@NonNull View itemView) {
            mmTvContent = (TextView) itemView.findViewById(R.id.tv_content);
        }
    }
}
