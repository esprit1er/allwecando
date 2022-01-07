package com.kratos.allwecando;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import androidx.annotation.RequiresApi;

public class MyAccessibilityService  extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";
    private AccessibilityNodeInfo rootInfoOld = null;
    private AccessibilityNodeInfo rootConfirmPurchase = null;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e(TAG, "onAccessibilityEvent: ");
        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
        //For market snip
       // marketClick(rootInfo);
        //for drop
        dropClick(rootInfo);
    }

    public void performclickOnParent(AccessibilityNodeInfo nodeParent){
        if (nodeParent != null && nodeParent.isClickable()  ){
            if (nodeParent.getChildCount()<3){
                nodeParent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }else {
                Log.e(TAG, "Node child count : "+nodeParent.getChildCount());
            }
        }else if ((nodeParent != null) && (nodeParent.isClickable() == false)){
            AccessibilityNodeInfo newnodeParent = nodeParent.getParent();
            performclickOnParent(newnodeParent);
        }
    }



    public void dropClick( AccessibilityNodeInfo rootInfo){
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
                    Thread.sleep(240000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else if ((nodeParent != null) && (nodeParent.isClickable() == false)){
            AccessibilityNodeInfo newnodeParent = nodeParent.getParent();
            performdropClick(newnodeParent);
        }
    }

    public void marketClick( AccessibilityNodeInfo rootInfo){
        List<AccessibilityNodeInfo> wallet =  rootInfo.findAccessibilityNodeInfosByText("Wallet Balance:");
        if (wallet.size()>0 && wallet.get(0).getParent() != null){
            for (AccessibilityNodeInfo node : wallet.get(0).getParent().findAccessibilityNodeInfosByText("BUY NOW"))
            {
                performclickMarketClick(node.getParent());
            }
        }else{
            for (AccessibilityNodeInfo node : rootInfo.findAccessibilityNodeInfosByText("BUY NOW"))
            {
                if (node.getParent() != null && (this.rootInfoOld == null || !node.getParent().equals(this.rootInfoOld))){
                    this.rootInfoOld = node.getParent();
                    performclickOnParent(node.getParent());
                }
            }
        }
    }

    public void performclickMarketClick(AccessibilityNodeInfo nodeParent){
        if (nodeParent != null && nodeParent.isClickable()  ){
            if (nodeParent.getChildCount()<3){
                nodeParent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                Log.e(TAG, "Node child count : "+nodeParent.getChildCount());
            }
        }else if ((nodeParent != null) && (nodeParent.isClickable() == false)){
            AccessibilityNodeInfo newnodeParent = nodeParent.getParent();
            performclickOnParent(newnodeParent);
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
