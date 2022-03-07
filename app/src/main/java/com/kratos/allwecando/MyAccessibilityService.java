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
    private AccessibilityNodeInfo itemMarket = null;

    private int mintBuy = 4000;
    private int testIncrement = 0;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
        containerCallMarket(rootInfo);
    }
    public void containerCallMarket(AccessibilityNodeInfo rootInfo){
        List<AccessibilityNodeInfo> detail = rootInfo.findAccessibilityNodeInfosByText("Details");
        if (detail.size()>0){
            marketClick(rootInfo);
        }else{
            snipMarket(rootInfo);
        }
    }

    /**
     * in buy list , rootinfo is Framlayout without resource-id
     * child0 is first chidl of rootinfo adn first viewgroup
     * viewGroup1 contain the scrollview with all list
     * @param rootInfo
     */
    public void snipMarket(AccessibilityNodeInfo rootInfo){
        List<AccessibilityNodeInfo> scrollViews = new ArrayList<>();
        findChildView(scrollViews,rootInfo);

        if (scrollViews.size()>0){
            AccessibilityNodeInfo scrollView = scrollViews.get(0);
            if (scrollView.getChildCount()>0){
                for (int j = 0; j< scrollView.getChildCount(); j++){
                    AccessibilityNodeInfo viewGroups = scrollView.getChild(j);
                    int mint = 0;
                    if (viewGroups!= null && viewGroups.getChildCount()>0){
                        for (int i = 0; i< viewGroups.getChildCount(); i++){
                            if (viewGroups.getChild(i) != null && "android.widget.TextView".equals(viewGroups.getChild(i).getClassName()) && viewGroups.getChild(i).getText() != null){
                                String text = viewGroups.getChild(i).getText().toString();
                                if (text.split("#").length>1){
                                    String mintText = text.split("#")[1];
                                    try {
                                        int mintNumber = Integer.parseInt(mintText);
                                        if (mintNumber<=this.mintBuy){
                                            mint = mintNumber;
                                        }
                                    }catch (Exception e){
                                        Log.e(TAG, "error: ", e);
                                    }
                                }
                            }
                        }
                    }

                    if ( mint != 0 && this.itemMarket != viewGroups && testIncrement == 0){
                        this.itemMarket = viewGroups;
                        testIncrement++;
                        viewGroups.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        break;
                    }
                }


            }
        }
    }

    private void findChildView( List<AccessibilityNodeInfo> listchild, AccessibilityNodeInfo roootnode){
        boolean endLoop = true;
        if (roootnode != null){
            for (int i = 0; i< roootnode.getChildCount() ; i++){
                if (roootnode.getChild(i) != null && "android.widget.ScrollView".equals(roootnode.getChild(i).getClassName())){
                    listchild.add(roootnode.getChild(i));
                    endLoop = false;
                    break;
                }
                if (endLoop){
                    findChildView(listchild,roootnode.getChild(i));
                }
            }
        }
    }

    public void marketClick( AccessibilityNodeInfo rootInfo){
        List<AccessibilityNodeInfo> wallet =  rootInfo.findAccessibilityNodeInfosByText("Wallet Balance:");
        if ((wallet.size()>0) && (wallet.get(0).getParent() != null)){
            for (AccessibilityNodeInfo node : wallet.get(0).getParent().findAccessibilityNodeInfosByText("BUY NOW"))
            {
                performclickMarketClick(node.getParent());
                break;
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            for (AccessibilityNodeInfo node : rootInfo.findAccessibilityNodeInfosByText("BUY NOW"))
            {
                if (node.getParent() != null && (this.rootInfoOld == null || !node.getParent().equals(this.rootInfoOld))){
                    testIncrement = 0;
                    this.rootInfoOld = node.getParent();
                    performclickMarketClick(node.getParent());
                }
            }
        }
    }

    public void performclickMarketClick(AccessibilityNodeInfo nodeParent){
        if (nodeParent != null){
            if (nodeParent.isClickable()  ){
                if (nodeParent.getChildCount()<3){
                    nodeParent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }else{
                performclickMarketClick(nodeParent.getParent());
            }
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
        info.eventTypes = AccessibilityEvent.TYPE_WINDOWS_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                            | AccessibilityEvent.TYPE_VIEW_CLICKED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

        info.notificationTimeout = 10;

        this.setServiceInfo(info);
        Log.d(TAG, "onServiceConnected: ");
    }
}
