package com.leelen.bleopendoor;

import com.clj.fastble.exception.BleException;

/**
 * Created by admin on 2017/12/11.
 */

public interface OpenDoorCallback {

    void onStart(int tryTime);

    void onRetry(int tryTime);

    void onFail(BleException e, int tryTime);

    void onSuccess(byte[] data, int tryTime);

    void onFinish(boolean success, int tryTime);

}
