# BleTest
自用

蓝牙直连门禁

使用方法：
        OpenTheDoorUtil.getInstance().setConnectChangeListener(new OpenTheDoorUtil.ConnectChangeListener() {
            @Override
            public void onDeviceConnect(BleDevice bleDevice) {
            }

            @Override
            public void onDisConnect() {
                System.out.println("++++onDisConnect+++++");
            }
        });

        OpenTheDoorUtil.getInstance().setOpenDoorCallback(new OpenDoorCallback() {
            @Override
            public void onStart(int tryTime) {
            }

            @Override
            public void onRetry(int tryTime) {
            }

            @Override
            public void onFail(BleException e, int tryTime) {
            }

            @Override
            public void onSuccess(byte[] data, int tryTime) {
            }

            @Override
            public void onFinish(boolean success, int tryTime) {
            }

        });
        OpenConfig config = new OpenConfig();
        config.setAutoConnect(false)
                .setScanTimeOut(3000)
                .setMaxTryTime(1)
                .setDeviceMac(mac)
                .build();
        OpenTheDoorUtil.getInstance().setConfig(config);
        OpenTheDoorUtil.getInstance().openTheDoor(data);
