package com.zhikong.tianlong;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.xpf.ch340_library.CH340Master;
import com.xpf.ch340_library.driver.InitCH340;
import com.xpf.ch340_library.runnable.ReadDataRunnable;
import com.xpf.ch340_library.utils.CH340Util;
import com.zhikong.tianlong.util.AssetUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, InitCH340.IUsbPermissionListener {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private List<LatLng> LatLngs;
    private static final String ACTION_USB_PERMISSION = "com.linc.USB_PERMISSION";
    private OverlayOptions overlayOptions;
    private View send, read;
    private TextView content;
    private boolean isFirst;//判断是否打开

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        send = findViewById(R.id.send);
        send.setOnClickListener(this);
        read = findViewById(R.id.read);
        read.setOnClickListener(this);
        content = findViewById(R.id.contont);
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
        mBaiduMap.setMapStatus(msu);
        mBaiduMap.setOnMapLongClickListener(listener);
        LatLngs = new ArrayList<>();

        initQuanXian();

    }
    private void initQuanXian(){
        boolean hasWrite=AssetUtils.isGrantExternalRW(this);
        if (hasWrite){
            runThread();
        }
    }
    private void runThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                initJsonFile();
            }
        }).start();
    }
    private void initJsonFile(){
        String targetFile="/sdcard/tianlong/data.txt";
        File file=new File(targetFile);
        if (file.exists()){
            try {
                JSONObject jsonObject=new JSONObject(AssetUtils.getFileFromSD(targetFile));
                JSONObject obj=jsonObject.getJSONObject("device");
                final int ProductId=obj.getInt("ProductId");
                final int VendorId=obj.getInt("VendorId");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initData(ProductId,VendorId);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else{
            try {
                AssetUtils.copy(this,"data.txt",targetFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            initJsonFile();
        }

    }

    private void initData(int pid,int vid) {
        InitCH340.setListener(this);
        InitCH340.ProductId=pid;
        InitCH340.VendorId=vid;
        InitCH340.setReadCallBack(new ReadDataRunnable.ReadCallBack() {
            @Override
            public void onReadCallBack(final String result) {
                if (content != null && result != null) {
                    content.post(new Runnable() {
                        @Override
                        public void run() {
                            content.setText("result-->" + result);
                        }
                    });

                }
            }
        });
        if (!isFirst) {
            isFirst = true;
            // 初始化 ch340-library
            CH340Master.initialize(this);
        }
    }

    BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
    BitmapDescriptor bdB = BitmapDescriptorFactory.fromResource(R.drawable.icon_markb);
    BitmapDescriptor bdC = BitmapDescriptorFactory.fromResource(R.drawable.icon_markc);
    BitmapDescriptor bdD = BitmapDescriptorFactory.fromResource(R.drawable.icon_markd);
    BitmapDescriptor bde = BitmapDescriptorFactory.fromResource(R.drawable.icon_marke);

    BaiduMap.OnMapLongClickListener listener = new BaiduMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng point) {
            if (LatLngs.size() < 5) {
                mBaiduMap.clear();
                LatLngs.add(point);
                for (int i = 0; i < LatLngs.size(); i++) {
                    BitmapDescriptor bitmapDescriptor = null;
                    int index = 0;
                    if (i == 0) {
                        bitmapDescriptor = bdA;
                        index = 9;
                    } else if (i == 1) {
                        bitmapDescriptor = bdB;
                        index = 8;
                    } else if (i == 2) {
                        bitmapDescriptor = bdC;
                        index = 7;
                    } else if (i == 3) {
                        bitmapDescriptor = bdD;
                        index = 6;
                    } else if (i == 4) {
                        bitmapDescriptor = bde;
                        index = 5;
                    }
                    MarkerOptions ooA = new MarkerOptions().position(LatLngs.get(i)).icon(bitmapDescriptor).zIndex(index).draggable(true);
//                    ooA.animateType(MarkerOptions.MarkerAnimateType.drop);
                    mBaiduMap.addOverlay(ooA);
                    if (LatLngs.size() > 3) {
                        overlayOptions = new PolygonOptions()
                                .points(LatLngs)
                                .stroke(new Stroke(5, 0xAA00FF00))
                                .fillColor(0xAAFFFF00);
                        mBaiduMap.addOverlay(overlayOptions);
                    }

                }
            }
        }
    };
//设置地图长按事件监听


    @Override
    protected void onPause() {
        // MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        // MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
        mMapView.onResume();
        super.onResume();
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, usbFilter);
    }

    @Override
    protected void onDestroy() {
        // MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
        mMapView.onDestroy();
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);

        // 回收 bitmap 资源
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.read:
                break;
            case R.id.send:
                sendData("xiaoxiaoxiao");
                break;

        }
    }

    @Override
    public void result(boolean isGranted) {
        if (!isGranted) {
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            InitCH340.getmUsbManager().requestPermission(InitCH340.getUsbDevice(), mPermissionIntent);
        }
    }


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Toast.makeText(MainActivity.this, "EXTRA_PERMISSION_GRANTED~", Toast.LENGTH_SHORT).show();
                            InitCH340.loadDriver(MainActivity.this, InitCH340.getmUsbManager());
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "EXTRA_PERMISSION_GRANTED null!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };
    private void sendData(String con) {
//        String string = "hex";

        if (!TextUtils.isEmpty(con)) {
            String format = "hex";
            int result= CH340Util.writeData(con.getBytes(), format);
            if (result==-1){
                Toast.makeText(MainActivity.this, "设备未连接", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "发送的数据不能为空！", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            //调用系统相机申请拍照权限回调
            case 101:
                runThread();
                break;
        }
    }
}
