package com.egar.scanner.api;

import juns.lib.media.scanner.IMediaScanListener;

/**
 * 播放服务API - 动作行为
 * <p>BIND扫描服务的Activity，需要实现该接口</p>
 *
 * @author Jun.Wang
 */
public interface IApiScannerActions
        extends EgarApiScanner.IEgarApiScanListener,  // API 用来监听扫描服务 BIND 和 UNBIND
        //IMediaScanService, // 扫描服务句柄，这里需要实现是为了规范统一的方法名，可自行定义
        //IDataOpActions,// 一些数据操作，如获取全量媒体数据等
        IMediaScanListener {// 扫描服务监听器，监听扫描状态、存储设备挂载状态变化、增量媒体数据等
}