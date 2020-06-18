package juns.lib.media.db.base;

import java.util.List;
import java.util.Map;

import juns.lib.media.bean.MediaBase;
import juns.lib.media.bean.StorageDevice;
import juns.lib.media.flags.MediaType;

/**
 * Database operate common actions
 *
 * @author Jun.Wang
 */
public interface DBActions {
    /**
     * 设置已挂载的存储设备集合
     *
     * @param mountedStorages 已挂载的存储设备集合
     */
    void bindMounted(List<StorageDevice> mountedStorages);

    /**
     * 查询对应媒体记录条数
     */
    long getCount();

    /**
     * Query medias
     *
     * @return MAP, key mediaUrl; value {@link MediaBase}
     */
    Map<String, ? extends MediaBase> getMapMedias();

    /**
     * Batch insertion of data
     *
     * @param listMedias MediaBase list.
     * @return int, count of inserted.
     */
    int insertNewMedias(final List<? extends MediaBase> listMedias);

    /**
     * 更新媒体
     *
     * @param type            1: Audio;
     *                        <p>2: Video;</p>
     *                        <p>3: Image.</p>
     *                        <p>See {@link MediaType}</p>
     *                        <p></p>
     * @param mediasToCollect 要执行更新的媒体对象,如更新音频Collect状态等。
     * @return int 返回更新的列ID,大于0表示更新成功.
     */
    int updateMediaCollect(int type, List mediasToCollect);

    /**
     * 删除历史收藏
     * <p>当新的存储盘插入后,一旦执行收藏动作，就需要清空已经移除的存储盘的历史收藏</p>
     *
     * @return 删除结果
     */
    int clearHistoryCollect();

    /**
     * Batch update media timestamp.
     *
     * @param mediaUrl Media path.
     * @return int, count of updated.
     */
    int updateMediaTimeStamp(String mediaUrl);

    /**
     * Update media path-related information according to media storageId.
     *
     * @param storageId Storage device unique identifier
     * @return >0 means execute successfully.
     */
    int updateMediaPathInfo(String storageId, String rootPath);
}
