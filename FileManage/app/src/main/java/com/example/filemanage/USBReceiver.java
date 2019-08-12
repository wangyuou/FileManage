package com.example.filemanage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class USBReceiver extends BroadcastReceiver {
    private static final String TAG = USBReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("接收到广播");
//        if(intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_ATTACHED"))
//        {
//            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//            Intent i = new Intent(context, MainActivity.class);
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            i.putExtra("ADDDeviceName",device.getDeviceName());
//            context.startActivity(i);
//        }
//        else if(intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_DETACHED"))
//        {
//            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//            Intent i = new Intent(context, MainActivity.class);
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            i.putExtra("RemoveDeviceName",device.getDeviceName());
//            context.startActivity(i);
//        }

//            String action = intent.getAction();
//            if(action.equals(Intent.ACTION_MEDIA_MOUNTED)){
//                String mountPath = intent.getData().getPath();
//                Log.d(TAG,"mountPath = "+mountPath);
//                if (!TextUtils.isEmpty(mountPath)) {
//                    //读取到U盘路径再做其他业务逻辑
//                }
//            } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED) || action.equals(Intent.ACTION_MEDIA_EJECT)) {
//                Toast.makeText(context, "No services information detected !", Toast.LENGTH_SHORT).show();
//            } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
//                //如果是开机完成，则需要调用另外的方法获取U盘的路径
//            }

    }
}