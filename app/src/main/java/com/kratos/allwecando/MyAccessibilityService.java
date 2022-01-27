package com.kratos.allwecando;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import androidx.annotation.RequiresApi;

public class MyAccessibilityService  extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";
    private AccessibilityNodeInfo rootInfoOld = null;
    private AccessibilityNodeInfo rootConfirmPurchase = null;



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e(TAG, "onAccessibilityEvent: "+AccessibilityEvent.obtain(event.getEventType()));
        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dropClick(rootInfo);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void dropClick(AccessibilityNodeInfo rootInfo){
        Instant start = Instant.now();
        for (AccessibilityNodeInfo node : rootInfo.findAccessibilityNodeInfosByText("Notify Me"))
        {
            performdropClick(node.getParent());
        }
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        Log.e(TAG, "Duration : "+timeElapsed.toMillis());
    }
    public void performdropClick(AccessibilityNodeInfo nodeParent){
        if (nodeParent != null && nodeParent.isClickable()  ){
            if (nodeParent.getChildCount()<3){
                nodeParent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                try {
                    Thread.sleep(239400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else if ((nodeParent != null) && (nodeParent.isClickable() == false)){
            AccessibilityNodeInfo newnodeParent = nodeParent.getParent();
            performdropClick(newnodeParent);
        }
    }


    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt: Something went wrong ");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

        info.notificationTimeout = 50;

        this.setServiceInfo(info);
        Log.d(TAG, "onServiceConnected: ");
    }
}
