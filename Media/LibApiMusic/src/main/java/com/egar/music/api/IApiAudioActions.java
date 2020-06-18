package com.egar.music.api;

import juns.lib.media.play.IAudioPlayListener;
import juns.lib.media.play.IAudioPlayService;

/**
 * 播放服务API - 动作行为
 * <p>BIND播放服务的Activity，需要实现该接口</p>
 *
 * @author Jun.Wang
 */
public interface IApiAudioActions
        extends EgarApiMusic.IEgarApiMusicListener, // API 用来监听扫描服务 BIND 和 UNBIND
        //IAudioPlayService, // 播放服务句柄
        //IAudioDataOpActions, // 一些数据操作，如获取全量媒体数据等
        IAudioPlayListener { // 播放服务监听器
}
