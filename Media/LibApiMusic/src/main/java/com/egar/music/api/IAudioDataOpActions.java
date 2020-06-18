package com.egar.music.api;

import java.util.List;
import java.util.Map;

import juns.lib.media.db.tables.AudioTables;

/**
 * 数据库操作行为统一定义
 */
public interface IAudioDataOpActions {

    /**
     * 查询媒体
     *
     * @param sortBy 0: Nothing;
     *               <p>1: Folder name;</p>
     *               <p>2: Media name;</p>
     *               <p>3: Artist name;</p>
     *               <p>4: Album name.</p>
     *               <p>See {@link juns.lib.media.flags.FilterType}</p>
     *               <p></p>
     * @param params 查询条件,统一为Like模糊匹配
     *               <p>params 为null表示查询所有数据</p>
     *               <p>Audios : [0]folderName,[1]mediaName,[2]fileName, [3]artistName,[4]albumName</p>
     *               <p>Videos or Images: [0]folderName,[1]mediaName,[2]fileName</p>
     * @return 媒体列表
     */
    List getAllMedias(int sortBy, String[] params);

    /**
     * 获取所有媒体信息
     *
     * @param whereColumns {{@link AudioTables.AudioInfoTable}} 列与值的映射集合;
     * @return 歌单类表
     */
    List getMediasByColumns(Map<String, String> whereColumns, String sortOrder);

    /**
     * 获取列表 - 文件夹
     *
     * @return {@link juns.lib.media.bean.FilterFolder}列表
     */
    List getFilterFolders();

    /**
     * 获取列表 - 艺术家/表演者
     *
     * @return {@link juns.lib.media.bean.FilterMedia}列表
     */
    List getFilterArtists();

    /**
     * 获取列表 - 专辑
     *
     * @return {@link juns.lib.media.bean.FilterMedia}列表
     */
    List getFilterAlbums();

    /**
     * 获取所有歌单
     *
     * @param id 歌单ID, ID==-1 表示查询所有的歌单;
     * @return 歌单类表
     */
    List getAllMediaSheets(int id);

    /**
     * 新增媒体歌单
     *
     * @param mediaSheet 要执行更新的媒体对象
     * @return int 返回更新的列ID,大于0表示更新成功.
     */
    int addMediaSheet(List mediaSheet);

    /**
     * 更新媒体歌单
     *
     * @param mediaSheet 要执行更新的媒体对象
     * @return int 返回更新的列ID,大于0表示更新成功.
     */
    int updateMediaSheet(List mediaSheet);

    /**
     * 查询所有 歌单->媒体 映射
     * <p>即获取 : 歌单ID 与 媒体路径 映射信息</p>
     *
     * @param sheetId 歌单ID, sheetId==-1 表示查询所有的映射信息;
     * @return 列表
     */
    List getAllMediaSheetMapInfos(int sheetId);

    /**
     * 新增媒体歌单与信息映射
     *
     * @param listMapInfos 映射列表信息
     * @return int 返回更新列总数，大于0表示更新成功。
     */
    int addMediaSheetMapInfos(List listMapInfos);

    /**
     * 删除 歌单-媒体 映射表
     *
     * @param sheetId 歌单ID
     * @return int, 返回更新的列ID, 大于0表示更新成功.
     */
    int deleteMediaSheetMapInfos(int sheetId);
}
