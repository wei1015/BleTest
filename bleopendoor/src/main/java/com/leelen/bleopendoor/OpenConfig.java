package com.leelen.bleopendoor;

import com.clj.fastble.scan.BleScanRuleConfig;

import java.util.UUID;

/**
 * Created by admin on 2017/12/11.
 */

public class OpenConfig {
    private BleScanRuleConfig.Builder builder;
    private BleScanRuleConfig config;

    private int maxTryTime = 0;
    private int receiveTimeOut = 2000;

    public OpenConfig() {
        builder = new BleScanRuleConfig.Builder();
    }

    public UUID[] getServiceUuids() {
        return config.getServiceUuids();
    }

    public String[] getDeviceNames() {
        return config.getDeviceNames();
    }

    public String getDeviceMac() {
        return config.getDeviceMac();
    }

    public boolean isAutoConnect() {
        return config.isAutoConnect();
    }

    public boolean isFuzzy() {
        return config.isFuzzy();
    }

    public long getScanTimeOut() {
        return config.getScanTimeOut();
    }

    public int getMaxTryTime() {
        return maxTryTime;
    }


    public int getReceiveTimeOut() {
        return receiveTimeOut;
    }

    public void setReceiveTimeOut(int receiveTimeOut) {
        this.receiveTimeOut = receiveTimeOut;
    }

    public BleScanRuleConfig getConfig() {
        return config;
    }

    public void setConfig(BleScanRuleConfig config) {
        this.config = config;
    }

    public OpenConfig setMaxTryTime(int maxTryTime) {
        this.maxTryTime = maxTryTime;
        return this;
    }

    public OpenConfig setServiceUuids(UUID[] uuids) {
        builder.setServiceUuids(uuids);
        return this;
    }

    public OpenConfig setDeviceName(boolean fuzzy, String... name) {
        builder.setDeviceName(fuzzy, name);
        return this;
    }

    public OpenConfig setDeviceMac(String mac) {
        builder.setDeviceMac(mac);
        return this;
    }

    public OpenConfig setAutoConnect(boolean autoConnect) {
        builder.setAutoConnect(autoConnect);
        return this;
    }

    public OpenConfig setScanTimeOut(long timeOut) {
        builder.setScanTimeOut(timeOut);
        return this;
    }


    public BleScanRuleConfig build() {
        config = builder.build();
        return config;
    }
}
