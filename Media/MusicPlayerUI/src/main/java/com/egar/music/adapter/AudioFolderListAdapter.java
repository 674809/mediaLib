package com.egar.music.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.egar.music.R;
import com.egar.music.utils.AudioUtils;

import java.util.ArrayList;
import java.util.List;

import juns.lib.android.utils.Logs;
import juns.lib.media.bean.FilterFolder;
import juns.lib.media.bean.FilterMedia;
import juns.lib.media.bean.ProAudio;
import juns.lib.media.flags.MediaCollectState;
import xskin.utils.SkinUtil;

public class AudioFolderListAdapter<T> extends BaseArrAdapter<T> implements SectionIndexer {
    // TAG
    private final String TAG = "AudioNameListAdapter";

    //
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    /**
     * Playing media.
     */
    private ProAudio mPlayingMedia;
    private String mPlayingMediaFolderPath = "";

    // Data list
    private List<T> mListData;

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

    public AudioFolderListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mNormalFontColor = context.getResources().getColor(android.R.color.white, null);
        mHlFontColor = context.getResources().getColor(R.color.music_item_selected_font, null);
    }

    public void setCollectListener(CollectListener l) {
        mCollectListener = l;
    }

    private void setListData(List<T> listData) {
        if (listData == null) {
            this.mListData = new ArrayList<>();
        } else {
            this.mListData = new ArrayList<>(listData);
        }
    }

    private void setPlayingMedia(ProAudio playingMedia) {
        try {
            this.mPlayingMedia = playingMedia;
            String playingMediaUrl = mPlayingMedia.getMediaUrl();
            this.mPlayingMediaFolderPath = playingMediaUrl.substring(0, playingMediaUrl.lastIndexOf("/"));
        } catch (Exception e) {
        }
    }

    public void refreshData(List<T> listData) {
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

    public void refreshData(List<T> listData, ProAudio playingMedia) {
        synchronized (this) {
            setPlayingMedia(playingMedia);
            setListData(listData);
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
    public T getItem(int position) {
        try {
            return mListData.get(position);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.activity_music_list_frag_folders_item, parent, false);
            holder = new ViewHolder();
            holder.ivStart = (ImageView) convertView.findViewById(R.id.iv_start);
            holder.tvIdx = (TextView) convertView.findViewById(R.id.tv_idx);
            holder.tvDesc = (TextView) convertView.findViewById(R.id.tv_desc);
            holder.vEnd = convertView.findViewById(R.id.v_end);
            holder.ivEnd = (ImageView) convertView.findViewById(R.id.iv_end);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //
        T tItem = getItem(position);
        if (tItem instanceof ProAudio) {
            //(1)
            holder.ivStart.setImageResource(R.drawable.icon_item_selected);
            //(2)
            holder.tvIdx.setText(String.valueOf((position + 1)));
            //(3)
            ProAudio item = (ProAudio) tItem;
            holder.tvDesc.setText(AudioUtils.getMediaTitle(mContext, -1, item, true));

            //(4)
            holder.ivEnd.setVisibility(View.VISIBLE);
            // Collect
            holder.vEnd.setOnClickListener(new CollectOnClick(holder.ivEnd, position));
            switch (item.getCollected()) {
                case MediaCollectState.COLLECTED:
                    holder.ivEnd.setImageResource(R.drawable.favor_c);
                    break;
                case MediaCollectState.UN_COLLECTED:
                    holder.ivEnd.setImageResource(R.drawable.favor_c_n);
                    break;
            }

            //Playing
            if (mPlayingMedia != null
                    && TextUtils.equals(mPlayingMedia.getMediaUrl(), item.getMediaUrl())) {
                holder.tvIdx.setTextColor(mHlFontColor);
                holder.tvDesc.setTextColor(mHlFontColor);
                holder.ivStart.setVisibility(View.VISIBLE);
            } else {
                holder.tvIdx.setTextColor(mNormalFontColor);
                holder.tvDesc.setTextColor(mNormalFontColor);
                holder.ivStart.setVisibility(View.INVISIBLE);
            }

        } else if (tItem instanceof FilterMedia) {
            //(1)
            holder.ivStart.setVisibility(View.VISIBLE);
            //(2)
            holder.tvIdx.setText(String.valueOf((position + 1)));
            //(3)
            FilterFolder filterMedia = (FilterFolder) tItem;
            holder.tvDesc.setText(filterMedia.sortStr);
            //(4)
            holder.ivEnd.setVisibility(View.INVISIBLE);

            try {
                String itemFolderPath = filterMedia.mediaFolder.getPath();
                if (TextUtils.equals(mPlayingMediaFolderPath, itemFolderPath)) {
                    holder.tvIdx.setTextColor(mHlFontColor);
                    holder.tvDesc.setTextColor(mHlFontColor);
                    holder.ivStart.setImageResource(R.drawable.icon_folder_c);
                } else {
                    holder.tvIdx.setTextColor(mNormalFontColor);
                    holder.tvDesc.setTextColor(mNormalFontColor);
                    holder.ivStart.setImageResource(R.drawable.icon_folder);
                }
            } catch (Exception e) {
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

    public int getNextPos() {
        int loop = getCount();
        int nextPos = mSelectedPos + 1;
        if (nextPos >= loop) {
            nextPos = 0;
        }
        return nextPos;
    }

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
                T item = getItem(idx);
                if (item == null) {
                    continue;
                }

                //
                if (item instanceof ProAudio) {
                    ProAudio media = (ProAudio) item;
                    char firstChar = media.getTitlePinYin().charAt(0);
                    if (firstChar == sectionIndex) {
                        position = idx;
                        break;
                    }
                } else if (item instanceof FilterMedia) {
                    FilterMedia filter = (FilterMedia) item;
                    char firstChar = filter.sortStrPinYin.charAt(0);
                    if (firstChar == sectionIndex) {
                        position = idx;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Logs.i(TAG, "getPositionForSection() >> e: " + e.getMessage());
        }
        return position;
    }

    @Override
    public int getSectionForPosition(int position) {
        int section = -1;
        try {
            T item = getItem(position);
            if (item instanceof ProAudio) {
                ProAudio media = (ProAudio) item;
                section = media.getTitlePinYin().charAt(0);
            } else if (item instanceof FilterMedia) {
                FilterMedia filter = (FilterMedia) item;
                section = filter.sortStrPinYin.charAt(0);
            }
        } catch (Exception e) {
            Logs.i(TAG, "getSectionForPosition() >> e: " + e.getMessage());
        }
        return section;
    }

    private final class ViewHolder {
        ImageView ivStart;
        TextView tvIdx, tvDesc;

        //Collect
        View vEnd;
        ImageView ivEnd;
    }
}
