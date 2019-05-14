package com.xpf.ch340_library.runnable;

import com.xpf.ch340_library.driver.InitCH340;
import com.xpf.ch340_library.logger.LogUtils;
import com.xpf.ch340_library.utils.CH340Util;

/**
 * Created by xpf on 2017/12/20.
 * Function:ReadDataRunnable
 */
public class ReadDataRunnable implements Runnable {

    private static final String TAG = "ReadDataRunnable";
    private boolean mStop = false; // 是否停止线程
    private ReadCallBack mReadCallBack;

    public ReadDataRunnable(ReadCallBack mReadCallBack) {
        this.mReadCallBack = mReadCallBack;
    }

    @Override
    public void run() {
        startReadThread();
    }

    /**
     * 开启读取数据线程
     */
    private void startReadThread() {
        while (!mStop) {
            byte[] receiveBuffer = new byte[32];// 接收数据数组
            // 读取缓存区的数据长度
            int length = InitCH340.getDriver().ReadData(receiveBuffer, 32);

            switch (length) {
                case 0: // 无数据
                    LogUtils.i(TAG, "No data~");
                    break;
                default: // 有数据时的处理
                    // 将此处收到的数组转化为HexString
                    String hexString = CH340Util.bytesToHexString(receiveBuffer, length);
                    LogUtils.i(TAG, "ReadHexString===" + hexString + ",length===" + length);
                   if (mReadCallBack!=null&&hexString!=null){
                       mReadCallBack.onReadCallBack(hexString);
                   }
                    break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止读取任务
     */
    public void stopTask() {
        mStop = true;
    }

    public interface ReadCallBack {
        void onReadCallBack(String result);
    }

}
