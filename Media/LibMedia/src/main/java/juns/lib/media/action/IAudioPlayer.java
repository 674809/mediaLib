package juns.lib.media.action;

/**
 * 播放器行为接口
 *
 * @author Jun.Wang
 */
public interface IAudioPlayer {
    /**
     * 播放指定路径的媒体
     *
     * @param mediaUrl 指定的媒体路径
     */
    void playMedia(String mediaUrl);

    /**
     * 播放媒体
     */
    void playMedia();

    /**
     * 暂停媒体播放
     */
    void pauseMedia();

    /**
     * 释放媒体播放器资源.
     * <p>需要销毁播放器对象，下次重新创建</p>
     */
    void releaseMedia();

    /**
     * 媒体是否正在播放中
     *
     * @return true，媒体播放中
     */
    boolean isMediaPlaying();

    /**
     * 设置播放媒体路径
     *
     * @param path 媒体路径
     */
    void setMediaPath(String path);

    /**
     * 获取当前媒体路径
     *
     * @return String, 媒体路径
     */
    String getMediaPath();

    /**
     * 获取当前媒体播放进度
     *
     * @return int, 毫秒
     */
    int getMediaTime();

    /**
     * 获取当前媒体总时长
     *
     * @return the duration in milliseconds, if no duration is available (for example, if streaming
     * * live content), -1 is returned.
     */
    int getMediaDuration();

    /**
     * 拖动到媒体指定时间进度
     *
     * @param millisecond
     */
    void seekMediaTo(int millisecond);

    /**
     * 拖动时异步的,这个方法给出了使用正在执行进度跳转。
     *
     * @return boolean true 正在执行跳转；false 跳转执行完毕
     */
    boolean isMediaSeeking();

    /**
     * 设置左右声道声音比率
     * <p>可以实现播放器内降音，不会影响到系统音量；</p>
     * <p>可以用来实现声音淡入淡出；</p>
     *
     * @param leftVolume  [0f~1f]
     * @param rightVolume [0f~1f]
     */
    void setVolume(float leftVolume, float rightVolume);
}
