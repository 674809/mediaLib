package com.egar.music.fragment;

import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.egar.music.R;
import com.egar.music.engine.EventBusDelegate;

import java.util.List;

import juns.lib.media.bean.FilterFolder;
import juns.lib.media.bean.FilterMedia;
import juns.lib.media.bean.ProAudio;

public abstract class BaseAudioFragment extends Fragment implements EventBusDelegate.EventBusCallback {

    //Frame loading animation images.
    protected final int[] LOADING_RES_ID_ARR = {R.drawable.loading_list0001,
            R.drawable.loading_list0002,
            R.drawable.loading_list0003,
            R.drawable.loading_list0004,
            R.drawable.loading_list0005,
            R.drawable.loading_list0006,
            R.drawable.loading_list0007,
            R.drawable.loading_list0008,
            R.drawable.loading_list0009,
            R.drawable.loading_list0010
    };

    /**
     * Fragment flags
     */
    public interface FragFlags {
        String FLAG_COLLECT = "collect";
        String FLAG_FOLDER = "folder";
        String FLAG_TITLE = "title";
        String FLAG_ARTIST = "artist";
        String FLAG_ALBUM = "album";
    }

    /**
     * Fragment 的标记
     *
     * @return @{@link FragFlags}
     */
    public String getFlag() {
        return "";
    }

    /**
     * 设置Fragment的参数
     *
     * @param params [0] 表示过滤参数，如歌名/表演者/专辑
     */
    public void setParams(String[] params) {
    }

    /**
     * Loading operation.
     *
     * @param isShow true means showing.
     */
    protected abstract void showLoading(boolean isShow);

    /**
     * 获取页面层级
     *
     * <p>0表示在最上层，即在[文件夹/表演者/专辑]第一层</p>
     * <p>1表示在第二层，即在[文件夹/表演者/专辑]第二层</p>
     * <p>[我的收藏/歌曲名]一直在第一层</p>
     */
    public abstract int getPageLayer();

    /**
     * Refresh playing
     *
     * @param playingMedia Media url that is playing.
     */
    public abstract void refreshPlaying(ProAudio playingMedia);

    /**
     * Scroll {@link android.widget.ListView} to playing position
     *
     * @param isWaitLoading If true means will wait sometime for ListView loading.
     */
    public abstract void scrollToPlayingPos(final boolean isWaitLoading);

    /**
     * Select previous
     */
    public abstract void selectPrev();

    /**
     * Select next media, not play.
     */
    public abstract void selectNext();

    /**
     * Play selected
     */
    public abstract void playSelected();

    /**
     * Scanning state callback.
     *
     * @param state : {@link juns.lib.media.flags.MediaScanState}
     */
    public abstract void onScanStateChanged(int state);

    /**
     * 增量数据回调
     *
     * @param listMedias 增量数据列表，根据需要转换对应的正确媒体列表
     */
    public abstract void onGotDeltaMedias(List listMedias);

    /**
     * Get position of mediaUrl in playing list.
     *
     * @param list     Media playing list.
     * @param mediaUrl Media path.
     * @return Position of mediaUrl in playing list.
     */
    protected int getPosAtPlayList(List<ProAudio> list, String mediaUrl) {
        int pos = -1;
        try {
            for (int LOOP = list.size(), idx = 0; idx < LOOP; idx++) {
                ProAudio media = list.get(idx);
                if (TextUtils.equals(media.getMediaUrl(), mediaUrl)) {
                    pos = idx;
                    break;
                }
            }
        } catch (Exception e) {
        }
        return pos;
    }

    /**
     * Get position of playing folder in folder list.
     *
     * @param list            Media folder list.
     * @param mediaFolderPath Media folder path.
     * @return Position of mediaUrl in folder list.
     */
    protected int getPosAtPlayFolders(List list, String mediaFolderPath) {
        int pos = -1;
        try {
            for (int LOOP = list.size(), idx = 0; idx < LOOP; idx++) {
                Object objItem = list.get(idx);
                FilterFolder filterFolder = (FilterFolder) objItem;
                if (TextUtils.equals(mediaFolderPath, filterFolder.mediaFolder.getPath())) {
                    pos = idx;
                    break;
                }
            }
        } catch (Exception e) {
        }
        return pos;
    }

    /**
     * Get position of playing folder in folder list.
     *
     * @param list   Media folder list.
     * @param artist Media artist.
     * @return Position of mediaUrl in folder list.
     */
    protected int getPosAtPlayArtists(List list, String artist) {
        int pos = -1;
        try {
            for (int LOOP = list.size(), idx = 0; idx < LOOP; idx++) {
                Object objItem = list.get(idx);
                FilterMedia filterMedia = (FilterMedia) objItem;
                if (TextUtils.equals(artist, filterMedia.sortStr)) {
                    pos = idx;
                    break;
                }
            }
        } catch (Exception e) {
        }
        return pos;
    }

    /**
     * Get position of playing folder in folder list.
     *
     * @param list  Media folder list.
     * @param album Media album.
     * @return Position of mediaUrl in folder list.
     */
    protected int getPosAtPlayAlbums(List list, String album) {
        int pos = -1;
        try {
            for (int LOOP = list.size(), idx = 0; idx < LOOP; idx++) {
                Object objItem = list.get(idx);
                FilterMedia filterMedia = (FilterMedia) objItem;
                if (TextUtils.equals(album, filterMedia.sortStr)) {
                    pos = idx;
                    break;
                }
            }
        } catch (Exception e) {
        }
        return pos;
    }

    /**
     * 根据数据位置，计算并获取该数据所在页的第一个数据索引
     */
    protected int getPageFirstPos(int idx) {
        int pageDelta = getPageItemCount();
        if (idx % pageDelta > 0) {
            return (idx / pageDelta) * pageDelta;
        }

        // 0 -> return 0;
        // 1 -> return 0;
        // 2 -> return 0;
        // 6 -> return 5;
        // 10 -> return 10;
        // 11 -> return 10;
        return idx;
    }

    /**
     * Get rows num of records per page.
     *
     * @return rows num of records per page.
     */
    protected int getPageItemCount() {
        return 5;
    }
}