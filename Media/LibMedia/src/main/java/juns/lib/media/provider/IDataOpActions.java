package juns.lib.media.provider;

import java.util.List;
import java.util.Map;

import juns.lib.media.bean.FilterFolder;
import juns.lib.media.bean.FilterMedia;
import juns.lib.media.db.tables.MediaTables;
import juns.lib.media.db.tables.AudioTables;
import juns.lib.media.db.tables.VideoTables;
import juns.lib.media.flags.FilterType;
import juns.lib.media.flags.MediaType;

/**
 * 数据库操作行为统一定义
 */
public interface IDataOpActions {

    /**
     * 查询媒体
     *
     * @param type   0: All types;
     *               <p>1: Audio;</p>
     *               <p>2: Video;</p>
     *               <p>3: Image.</p>
     *               <p>See {@link MediaType}</p>
     *               <p></p>
     * @param sortBy 0: Nothing;
     *               <p>1: Folder name;</p>
     *               <p>2: Media name;</p>
     *               <p>3: Artist name;</p>
     *               <p>4: Album name.</p>
     *               <p>See {@link FilterType}</p>
     *               <p></p>
     * @param params 查询条件,统一为Like模糊匹配
     *               <p>params 为null表示查询所有数据</p>
     *               <p>type==Audio: [0]folderName,[1]mediaName,[2]fileName, [3]artistName,[4]albumName,[5]collected</p>
     *               <p>type==Video: [0]folderName,[1]mediaName,[2]fileName</p>
     *               <p>type==Image: [0]folderName,[1]mediaName,[2]fileName</p>
     * @return 媒体列表
     */
    List getAllMedias(int type, int sortBy, String[] params);

    /**
     * 获取所有媒体信息
     *
     * @param type         {@link MediaType}
     * @param whereColumns {{@link MediaTables.MediaInfoTable}
     *                     / {@link AudioTables.AudioInfoTable}
     *                     / {@link VideoTables.VideoInfoTable}} 查询条件的[列 映射 值];
     * @return 歌单类表
     */
    List getMediasByColumns(int type, Map<String, String> whereColumns, String sortOrder);

    /**
     * 获取列表 - 文件夹
     *
     * @param type {@link MediaType}
     * @return {@link FilterFolder}列表
     */
    List getFilterFolders(int type);

    /**
     * 获取列表 - 艺术家/表演者
     *
     * @param type {@link MediaType}
     * @return {@link FilterMedia}列表
     */
    List getFilterArtists(int type);

    /**
     * 获取列表 - 专辑
     *
     * @param type {@link MediaType}
     * @return {@link FilterMedia}列表
     */
    List getFilterAlbums(int type);

    /**
     * 获取所有歌单
     *
     * @param type <p>1: Audio;</p>
     *             <p>2: Video;</p>
     *             <p>3: Image.</p>
     *             <p>See {@link ;juns.lib.media.flags.MediaType}</p>
     *             <p></p>
     * @param id   歌单ID, ID==-1 表示查询所有的歌单;
     * @return 歌单类表
     */
    List getAllMediaSheets(int type, int id);

    /**
     * 新增媒体歌单
     *
     * @param type       <p>1: Audio;</p>
     *                   <p>2: Video;</p>
     *                   <p>3: Image.</p>
     *                   <p>See {@link ;juns.lib.media.flags.MediaType}</p>
     *                   <p></p>
     * @param mediaSheet 要执行更新的媒体对象
     * @return int 返回更新的列ID,大于0表示更新成功.
     */
    int addMediaSheet(int type, List mediaSheet);

    /**
     * 更新媒体歌单
     *
     * @param type       1: Audio;
     *                   <p>2: Video;</p>
     *                   <p>3: Image.</p>
     *                   <p>See {@link ;juns.lib.media.flags.MediaType}</p>
     *                   <p></p>
     * @param mediaSheet 要执行更新的媒体对象
     * @return int 返回更新的列ID,大于0表示更新成功.
     */
    int updateMediaSheet(int type, List mediaSheet);

    /**
     * 查询所有 歌单->媒体 映射
     * <p>即获取 : 歌单ID 与 媒体路径 映射信息</p>
     *
     * @param type    <p>1: Audio;</p>
     *                <p>2: Video;</p>
     *                <p>3: Image.</p>
     *                <p>See {@link ;juns.lib.media.flags.MediaType}</p>
     *                <p></p>
     * @param sheetId 歌单ID, sheetId==-1 表示查询所有的映射信息;
     * @return 列表
     */
    List getAllMediaSheetMapInfos(int type, int sheetId);

    /**
     * 新增媒体歌单与信息映射
     *
     * @param type         <p>1: Audio;</p>
     *                     <p>2: Video;</p>
     *                     <p>3: Image.</p>
     *                     <p>See {@link ;juns.lib.media.flags.MediaType}</p>
     *                     <p></p>
     * @param listMapInfos 映射列表信息
     * @return int 返回更新列总数，大于0表示更新成功。
     */
    int addMediaSheetMapInfos(int type, List listMapInfos);

    /**
     * 删除 歌单-媒体 映射表
     *
     * @param type    1: Audio;
     *                <p>2: Video;</p>
     *                <p>3: Image.</p>
     *                <p>See {@link MediaType}</p>
     *                <p></p>
     * @param sheetId 歌单ID
     * @return int, 返回更新的列ID, 大于0表示更新成功.
     */
    int deleteMediaSheetMapInfos(int type, int sheetId);
}
