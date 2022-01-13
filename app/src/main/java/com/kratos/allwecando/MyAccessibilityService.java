package com.kratos.allwecando;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.RequiresApi;

public class MyAccessibilityService  extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";
    private AccessibilityNodeInfo rootInfoOld = null;
    private AccessibilityNodeInfo rootConfirmPurchase = null;
    private long globalRemaintime = 0;
    public static final String TIME_SERVER = "kwynn.com";
    private boolean mustClick = true;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e(TAG, "onAccessibilityEvent: "+event.getEventType());

        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
        //For market snip
        marketClick(rootInfo);
        //for drop
       // dropClick(rootInfo);

        //Comics drop test
       // dropClickTest(rootInfo);


    }

    public long remainTime(Date date1) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        //Here you set to your timezone
        sdf.setTimeZone(TimeZone.getDefault());

        //Date date1 = sdf.parse(sdf.format(calendar.getTime()));
        Date date2 = sdf.parse("2022-01-11 05:00:01.000");
        long remainTime = date2.getTime() - date1.getTime();
        Log.e(TAG, "remainTime: "+remainTime );

        //Delay between
        return remainTime;
    }

    public Date testGlobalTime() throws IOException {
        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
        TimeInfo timeInfo = timeClient.getTime(inetAddress);
        long returnTime = timeInfo.getReturnTime();
        Date time = new Date(returnTime);
       // Log.e(TAG, "testGlobalTime: Time from " + TIME_SERVER + ": " + time);
        return time;
    }
    public void dropClickTest( AccessibilityNodeInfo rootInfo){
        for (AccessibilityNodeInfo node : rootInfo.findAccessibilityNodeInfosByText("6.99"))
        {
            try {
                performdropClickComicsTest(node.getParent());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void performdropClickComicsTest(AccessibilityNodeInfo nodeParent) throws ParseException {
        if (nodeParent != null && nodeParent.isClickable()  ){
            if (nodeParent.getChildCount()<3){
                if (mustClick){
                    mustClick = false;
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                try {
                                    Thread.sleep(remainTime(testGlobalTime()));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                nodeParent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }else if ((nodeParent != null) && (nodeParent.isClickable() == false)){
            AccessibilityNodeInfo newnodeParent = nodeParent.getParent();
            performdropClick(newnodeParent);
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
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            for (AccessibilityNodeInfo node : rootInfo.findAccessibilityNodeInfosByText("BUY NOW"))
            {
                if (node.getParent() != null && (this.rootInfoOld == null || !node.getParent().equals(this.rootInfoOld))){
                    this.rootInfoOld = node.getParent();
                    performclickMarketClick(node.getParent());
                }
            }
        }
    }

    public void performclickMarketClick(AccessibilityNodeInfo nodeParent){
        if (nodeParent != null && nodeParent.isClickable()  ){
            if (nodeParent.getChildCount()<3){
                nodeParent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
               /* try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }else {
                Log.e(TAG, "Node child count : "+nodeParent.getChildCount());
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
       // info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

        info.notificationTimeout = 50;

        this.setServiceInfo(info);
        Log.d(TAG, "onServiceConnected: ");
    }

    public void getDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        //Here you set to your timezone
        sdf.setTimeZone(TimeZone.getDefault());
        //Will print on your default Timezone
        Log.e(TAG, "getDate: "+sdf.format(calendar.getTime()) );
    }



    public boolean checkDateSup(Date d1, Date d2){
        int result = d1.compareTo(d2);
        if (result>0){
            return true;
        }
        return false;
    }

    public boolean clickable() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        //Here you set to your timezone
        sdf.setTimeZone(TimeZone.getDefault());

        Date date1 = sdf.parse(sdf.format(calendar.getTime()));
        Date date2 = sdf.parse("2022-01-11 14:08:59.652");

        return checkDateSup(date1,date2);
    }
}
