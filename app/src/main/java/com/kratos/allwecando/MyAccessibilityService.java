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

    private double pricebuy = 7.00;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e(TAG, "onAccessibilityEvent: "+event.getEventType());
        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
        containerCallMarketWithPrice(rootInfo);


    }

    public void containerCallMarketWithPrice(AccessibilityNodeInfo rootInfo){
        if (rootInfo != null){
            List<AccessibilityNodeInfo> detail = rootInfo.findAccessibilityNodeInfosByText("Details");
            if (detail.size()>0){
              marketClick(rootInfo);
            }else{
              snipMarketPrice(rootInfo);
            }
        }
    }


    public void snipMarketPrice(AccessibilityNodeInfo rootInfo){
        findChildViewByPrice(rootInfo);
    }
    public void findChildViewByPrice( AccessibilityNodeInfo roootnode){
        if (roootnode != null){
            for (int i = 0; i< roootnode.getChildCount() ; i++){
                if (roootnode.getChild(i) != null && "android.widget.ScrollView".equals(roootnode.getChild(i).getClassName())){
                    testSnipMarketByPrice(roootnode.getChild(i));
                    break;
                }else{
                    findChildViewByPrice(roootnode.getChild(i));
                }
            }
        }
    }
    public void testSnipMarketByPrice(AccessibilityNodeInfo scrollView){
        if (scrollView.getChildCount()>1){
            for (int j = 0; j< scrollView.getChildCount(); j++){
                AccessibilityNodeInfo viewGroups = scrollView.getChild(j);
                double priceMarket = 0.0;
                if (viewGroups!= null && viewGroups.getChildCount()>0){
                    for (int i = 0; i< viewGroups.getChildCount(); i++){
                        if (viewGroups.getChild(i) != null && "android.widget.TextView".equals(viewGroups.getChild(i).getClassName()) && viewGroups.getChild(i).getText() != null){
                            String text = viewGroups.getChild(i).getText().toString();
                            if (isNumeric(text)){
                                try {
                                    priceMarket = Double.parseDouble(text);
                                    if ((priceMarket <= pricebuy) && (priceMarket != 0.0)){
                                        break;
                                    }
                                }catch (Exception e){
                                    Log.e(TAG, e.toString() );
                                }
                            }
                        }
                    }
                }
                if ((priceMarket <= pricebuy) && (priceMarket != 0.0) ){
                    viewGroups.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
            }
        }
    }

    public void marketClick( AccessibilityNodeInfo rootInfo){
        for (AccessibilityNodeInfo node : rootInfo.findAccessibilityNodeInfosByText("BUY NOW"))
        {
            if (node.getParent() != null && (this.rootInfoOld == null || !node.getParent().equals(this.rootInfoOld))){
                this.rootInfoOld = node.getParent();
                performclickMarketClick(node.getParent());
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void performclickMarketClick(AccessibilityNodeInfo nodeParent){
        if (nodeParent != null && nodeParent.isClickable()  ){
            if (nodeParent.getChildCount()<3){
                nodeParent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }else if ((nodeParent != null) && (nodeParent.isClickable() == false)){
            AccessibilityNodeInfo newnodeParent = nodeParent.getParent();
            performclickMarketClick(newnodeParent);
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

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
