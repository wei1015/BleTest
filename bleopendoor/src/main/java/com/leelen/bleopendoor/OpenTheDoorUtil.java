package com.leelen.bleopendoor;

import android.bluetooth.BluetoothGatt;
import android.os.Handler;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanAndConnectCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.exception.NotFoundDeviceException;
import com.clj.fastble.exception.OtherException;
import com.clj.fastble.exception.TimeoutException;


/**
 * Created by admin on 2017/12/11.
 */

public class OpenTheDoorUtil {

    public enum Status {
        none, scanning, scanEnd, connecting, connectEnd, notify, notifyEnd, write, writeEnd, retry, finish
    }

    final String chatServiceUuid = "00001919-0000-1000-8000-00805f9b34fb"; // 蓝牙服务固定uuid
    final String chatBleGattCharacteristicUuid = "00002c2c-0000-1000-8000-00805f9b34fb"; // 蓝牙标签固定uuid
    private static OpenTheDoorUtil instance;
    private OpenConfig config;
    private ConnectChangeListener connectChangeListener;
    private OpenDoorCallback openDoorCallback;
    private BleScanAndConnectImpl scanAndConnectImpl;
    private BleNotifyImpl notifyImpl;
    private BleWriteImpl writeImpl;
    private BleDevice connectDevice;
    private byte[] data;


    private Status action;

    private int tryTime;


    public static OpenTheDoorUtil getInstance() {
        if (instance == null) {
            instance = new OpenTheDoorUtil();
        }
        return instance;
    }

    Handler handler = new Handler();

    private OpenTheDoorUtil() {
        config = new OpenConfig();
        scanAndConnectImpl = new BleScanAndConnectImpl();
        notifyImpl = new BleNotifyImpl();
        writeImpl = new BleWriteImpl();
    }

    public ConnectChangeListener getConnectChangeListener() {
        return connectChangeListener;
    }

    public void setConnectChangeListener(ConnectChangeListener connectChangeListener) {
        this.connectChangeListener = connectChangeListener;
    }

    public OpenDoorCallback getOpenDoorCallback() {
        return openDoorCallback;
    }

    public void setOpenDoorCallback(OpenDoorCallback openDoorCallback) {
        this.openDoorCallback = openDoorCallback;
    }


    public OpenConfig getConfig() {
        return config;
    }

    public void setConfig(OpenConfig config) {
        this.config = config;
    }

    public void openTheDoor(byte[] data) {
        tryTime = 0;
        this.data = data;
        BleManager.getInstance().enableLog(false);
        BleManager.getInstance().initScanRule(config.getConfig());
        start();
    }

    /**
     * 开始执行，重新执行也会调用该方法
     */
    private void start() {
        BleManager.getInstance().disconnectAllDevice();
        if (openDoorCallback != null) {
            openDoorCallback.onStart(tryTime);
        }
        actionChanged(Status.none);
        BleManager.getInstance().scanAndConnect(scanAndConnectImpl);
    }

    /**
     * 有设置重试则会启动该方法
     */
    private void retry() {
        if (tryTime < config.getMaxTryTime()) {
            tryTime++;
            actionChanged(Status.retry);
            if (openDoorCallback != null)
                openDoorCallback.onRetry(tryTime);
            start();
        } else {
            onFinish(false, tryTime);
        }
    }

    /**
     * 订阅蓝牙返回的消息
     *
     * @param device
     */
    private void indicate(BleDevice device) {
        actionChanged(Status.notify);
        BleManager.getInstance().notify(device, chatServiceUuid, chatBleGattCharacteristicUuid, notifyImpl);
    }

    /**
     * 向设备写入数据
     */
    private void write() {
        if (connectDevice != null) {
            actionChanged(Status.write);
            BleManager.getInstance().write(connectDevice, chatServiceUuid, chatBleGattCharacteristicUuid, data, writeImpl);
        } else {
            onFail(new NotFoundDeviceException());
            retry();
        }
    }

    /**
     * 出现失败的时候
     *
     * @param exception
     */
    private void onFail(BleException exception) {
        if (openDoorCallback != null)
            openDoorCallback.onFail(exception, tryTime);
    }


    private void onSuccess(byte[] data) {
        if (openDoorCallback != null)
            openDoorCallback.onSuccess(data, tryTime);
    }


    private void onFinish(boolean success, int tryTime) {
        actionChanged(Status.finish);
        handler.removeCallbacks(timeOutAct);
        if (openDoorCallback != null)
            openDoorCallback.onFinish(success, tryTime);
    }

    private void deviceStatusChange(BleDevice device) {
        connectDevice = device;
        if (connectChangeListener != null) {
            if (device != null) {
                connectChangeListener.onDeviceConnect(device);
            } else {
                connectChangeListener.onDisConnect();
            }
        }
    }

    private void actionChanged(Status status) {
        action = status;
        System.out.println("status:" + action);
    }

    Runnable timeOutAct = new Runnable() {
        @Override
        public void run() {
            onFail(new TimeoutException());
            retry();
            handler.removeCallbacks(timeOutAct);
//            BleManager.getInstance().disconnectAllDevice();
//            retry();
        }
    };

    class BleScanAndConnectImpl extends BleScanAndConnectCallback {

        @Override
        public void onScanStarted(boolean success) {
            if (!success) {
                onFail(new OtherException("Scan Fail Exception Occurred! "));
            } else {
                actionChanged(Status.scanning);
            }
        }

        @Override
        public void onScanFinished(BleDevice scanResult) {
            actionChanged(Status.scanEnd);
            connectDevice = null;
            if (scanResult == null) {
                onFail(new NotFoundDeviceException());
                retry();
            }
        }

        @Override
        public void onStartConnect() {
            actionChanged(Status.connecting);
        }

        @Override
        public void onConnectFail(BleException exception) {
            actionChanged(Status.connectEnd);
            onFail(exception);
            retry();
        }

        @Override
        public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
            actionChanged(Status.connectEnd);
            deviceStatusChange(bleDevice);
            indicate(bleDevice);
        }

        @Override
        public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
            deviceStatusChange(null);
        }
    }

    class BleNotifyImpl extends BleNotifyCallback {

        @Override
        public void onNotifySuccess() {
            actionChanged(Status.notifyEnd);
            write();
        }

        @Override
        public void onNotifyFailure(BleException exception) {
            actionChanged(Status.notifyEnd);
            onFail(exception);
            retry();
        }

        @Override
        public void onCharacteristicChanged(byte[] data) {
            actionChanged(Status.finish);
            onSuccess(data);
            onFinish(true, tryTime);
            BleManager.getInstance().disconnectAllDevice();
        }
    }


    class BleWriteImpl extends BleWriteCallback {

        @Override
        public void onWriteSuccess() {
            actionChanged(Status.writeEnd);
            handler.postDelayed(timeOutAct, config.getReceiveTimeOut());
        }

        @Override
        public void onWriteFailure(BleException exception) {
            actionChanged(Status.writeEnd);
            onFail(exception);
            retry();
        }
    }


    public interface ConnectChangeListener {
        void onDeviceConnect(BleDevice bleDevice);

        void onDisConnect();
    }

    public void destroy() {
        BleManager.getInstance().destroy();
        config = null;
        scanAndConnectImpl = null;
        notifyImpl = null;
        writeImpl = null;
        openDoorCallback = null;
        connectChangeListener = null;
        connectDevice = null;

    }

}
