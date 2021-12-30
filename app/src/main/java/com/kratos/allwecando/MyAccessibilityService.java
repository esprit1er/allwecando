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
        String packageName = event.getPackageName().toString();
        Log.e(TAG, "getPackageName: "+packageName);
        PackageManager packageManager = this.getPackageManager();

        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();


        /**
         * Section for Market buy now
         */
        /*
        Log.e(TAG, "rootInfoOld is null");
        List<AccessibilityNodeInfo> wallet =  rootInfo.findAccessibilityNodeInfosByText("Wallet Balance:");

        if (wallet.size()>0 && wallet.get(0).getParent() != null && !wallet.get(0).getParent().equals(rootConfirmPurchase)){
            rootConfirmPurchase = wallet.get(0).getParent();
            for (AccessibilityNodeInfo node : wallet.get(0).getParent().findAccessibilityNodeInfosByText("BUY NOW"))
            {
                Log.e(TAG, "Node list : "+node.getText());
                Rect bounds = new Rect();
                node.getBoundsInScreen(bounds);
                AccessibilityNodeInfo nodeParent1 = node.getParent();
                performclickOnParent(nodeParent1);
            }
        }else{
            for (AccessibilityNodeInfo node : rootInfo.findAccessibilityNodeInfosByText("BUY NOW"))
            {
                Log.e(TAG, "Node list : "+node.getText());
                Rect bounds = new Rect();
                node.getBoundsInScreen(bounds);
                AccessibilityNodeInfo nodeParent1 = node.getParent();

                if (nodeParent1 != null && (this.rootInfoOld == null || !nodeParent1.equals(this.rootInfoOld))){
                    this.rootInfoOld = nodeParent1;
                    performclickOnParent(nodeParent1);
                }
            }
        }*/
        /**
         * Section For drop
         */

        for (AccessibilityNodeInfo node : rootInfo.findAccessibilityNodeInfosByText("BUY NOW"))
        {
            Log.e(TAG, "Node list : "+node.getText());
            Rect bounds = new Rect();
            node.getBoundsInScreen(bounds);
            AccessibilityNodeInfo nodeParent1 = node.getParent();
            performclickDropOnParent(nodeParent1);
        }
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

    public void performclickDropOnParent(AccessibilityNodeInfo nodeParent){
        if (nodeParent != null && nodeParent.isClickable()  ){
            if (nodeParent.getChildCount()<3){
                nodeParent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                //Comment code below for snip in the market
                try {
                    Thread.sleep(240000);
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
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
        AccessibilityEvent.TYPE_VIEW_FOCUSED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

        info.notificationTimeout = 100;

        this.setServiceInfo(info);
        Log.d(TAG, "onServiceConnected: ");
    }
}
