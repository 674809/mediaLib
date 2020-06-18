package com.egar.scanner.service.presenter;

import juns.lib.media.scanner.IMediaScanService;
import juns.lib.media.utils.StorageManger;

/**
 * Scan presenter.
 *
 * @author Jun.Wang
 */
public abstract class IScanPresenter extends IMediaScanService.Stub
        implements StorageManger.StorageManagerListener {
}