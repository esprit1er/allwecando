package com.kratos.allwecando;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.RequiresApi;

public class MyAccessibilityService  extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //Log.e(TAG, "onAccessibilityEvent: "+AccessibilityEvent.obtain(event.getEventType()));
        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
        dropClick(rootInfo);

    }

    public void dropClick(AccessibilityNodeInfo rootInfo){
        for (AccessibilityNodeInfo node : rootInfo.findAccessibilityNodeInfosByText("BUY NOW"))
        {
            performdropClick(node.getParent());
        }
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

        info.notificationTimeout = 10;

        this.setServiceInfo(info);
        Log.d(TAG, "onServiceConnected: ");
    }
}
