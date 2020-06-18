package com.egar.music.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.egar.music.R;
import com.egar.music.adapter.BaseArrAdapter.CollectListener;
import com.egar.music.utils.AudioUtils;

import java.util.ArrayList;
import java.util.List;

import juns.lib.android.utils.Logs;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.flags.MediaCollectState;
import xskin.utils.SkinUtil;

/**
 * Audio names list adapter - [Song name]
 *
 * @author Jun.Wang
 */
public class AudioCollectListAdapter extends ArrayAdapter<ProAudio> implements SectionIndexer {
    // TAG
    private final String TAG = "AudioNameListAdapter";

    //
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    // Data list
    private List<ProAudio> mListData;

    /**
     * Playing media.
     */
    private ProAudio mPlayingMedia;

    /**
     * Select position, not always equal to playing position.
     */
    private int mSelectedPos = -1;

    /**
     * Color of item font.
     * <P>Playing : mHlFontColor</P>
     * <P>Normal : mNormalFontColor</P>
     */
    private int mHlFontColor, mNormalFontColor;

    /**
     * {@link CollectListener} object.
     */
    private CollectListener mCollectListener;

    /**
     * Constructor
     *
     * @param context {@link Context}
     */
    public AudioCollectListAdapter(Context context) {
        super(context, 0);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mNormalFontColor = context.getResources().getColor(android.R.color.white, null);
        mHlFontColor = context.getResources().getColor(R.color.music_item_selected_font, null);
    }

    public void setCollectListener(CollectListener l) {
        mCollectListener = l;
    }

    private void setListData(List<ProAudio> listData) {
        if (listData == null) {
            this.mListData = new ArrayList<>();
        } else {
            this.mListData = new ArrayList<>(listData);
        }
    }

    private void setPlayingMedia(ProAudio playingMedia) {
        this.mPlayingMedia = playingMedia;
    }

    public void refreshData(List<ProAudio> listData, ProAudio playingMedia) {
        synchronized (this) {
            setPlayingMedia(playingMedia);
            setListData(listData);
            refreshData();
        }
    }

    public void refreshData(List<ProAudio> listData) {
        synchronized (this) {
            setListData(listData);
            refreshData();
        }
    }

    public void refreshPlaying(ProAudio playingMedia) {
        synchronized (this) {
            setPlayingMedia(playingMedia);
            refreshData();
        }
    }

    public void select(int pos) {
        synchronized (this) {
            mSelectedPos = pos;
            refreshData();
        }
    }

    public void refreshData() {
        notifyDataSetChanged();
    }


    public int getSelectPos() {
        return mSelectedPos;
    }

    public void resetSelect() {
        mSelectedPos = -1;
    }

    @Override
    public int getCount() {
        if (mListData == null) {
            return 0;
        }
        return mListData.size();
    }

    @Override
    public ProAudio getItem(int position) {
        try {
            return mListData.get(position);
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.activity_music_list_frag_collects_item, parent, false);
            holder.ivPlaying = (ImageView) convertView.findViewById(R.id.iv_start);
            holder.tvIdx = (TextView) convertView.findViewById(R.id.tv_idx);
            holder.tvDesc = (TextView) convertView.findViewById(R.id.tv_desc);
            holder.vEnd = convertView.findViewById(R.id.v_end);
            holder.ivEnd = (ImageView) convertView.findViewById(R.id.iv_end);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //
        ProAudio item = getItem(position);
        if (item != null) {
            //
            holder.tvIdx.setText(String.valueOf((position + 1)));
            holder.tvDesc.setText(AudioUtils.getMediaTitle(mContext, -1, item, true));

            //Playing
            if (mPlayingMedia != null
                    && TextUtils.equals(mPlayingMedia.getMediaUrl(), item.getMediaUrl())) {
                holder.tvIdx.setTextColor(mHlFontColor);
                holder.tvDesc.setTextColor(mHlFontColor);
                holder.ivPlaying.setVisibility(View.VISIBLE);
            } else {
                holder.tvIdx.setTextColor(mNormalFontColor);
                holder.tvDesc.setTextColor(mNormalFontColor);
                holder.ivPlaying.setVisibility(View.INVISIBLE);
            }

            //Collect
            holder.vEnd.setOnClickListener(new CollectOnClick(holder.ivEnd, position));
            switch (item.getCollected()) {
                case MediaCollectState.COLLECTED:
                    holder.ivEnd.setImageResource(R.drawable.favor_c);
                    break;
                case MediaCollectState.UN_COLLECTED:
                    holder.ivEnd.setImageResource(R.drawable.favor_c_n);
                    break;
            }
        }

        //Item background
        if (mSelectedPos == position) {
            convertView.setBackground(SkinUtil.instance().getDrawable(R.drawable.bg_lv_item_selected));
        } else {
            convertView.setBackgroundResource(0);
        }
        return convertView;
    }

    /**
     * Collect icon click event
     */
    private class CollectOnClick implements View.OnClickListener {
        private ImageView ivCollect;
        private int mmPosition;

        CollectOnClick(ImageView iv, int position) {
            ivCollect = iv;
            mmPosition = position;
        }

        @Override
        public void onClick(View v) {
            if (mCollectListener != null) {
                mCollectListener.onClickCollectBtn(ivCollect, mmPosition);
            }
        }
    }

    /**
     * Next position of selected position.
     */
    public int getNextPos() {
        int loop = getCount();
        int nextPos = mSelectedPos + 1;
        if (nextPos >= loop) {
            nextPos = 0;
        }
        return nextPos;
    }

    /**
     * Next position of selected position.
     */
    public int getPrevPos() {
        int prevPos = mSelectedPos - 1;
        if (prevPos < 0) {
            prevPos = getCount() - 1;
        }
        return prevPos;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        int position = -1;
        try {
            for (int idx = 0; idx < getCount(); idx++) {
                ProAudio media = getItem(idx);
                if (media == null) {
                    continue;
                }

                //
                char firstChar = media.getTitlePinYin().charAt(0);
                if (firstChar == sectionIndex) {
                    position = idx;
                    break;
                }
            }
        } catch (Exception e) {
            Logs.i(TAG, "getPositionForSection() >> " + e.getMessage());
        }
        return position;
    }

    @Override
    public int getSectionForPosition(int position) {
        int section = -1;
        try {
            ProAudio media = getItem(position);
            if (media != null) {
                section = media.getTitlePinYin().charAt(0);
            }
        } catch (Exception e) {
            Logs.i(TAG, "getSectionForPosition() >> " + e.getMessage());
        }
        return section;
    }

    private final class ViewHolder {
        ImageView ivPlaying;
        TextView tvIdx, tvDesc;

        //Collect
        View vEnd;
        ImageView ivEnd;
    }
}
