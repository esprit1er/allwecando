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
    private AccessibilityNodeInfo rootConfirmPurchase = null;
    private AccessibilityNodeInfo itemMarket = null;
    private long globalRemaintime = 0;
    public static final String TIME_SERVER = "kwynn.com";
    private boolean mustClick = true;

    private double pricebuy = 25.00;
    private int mintBuy = 4000;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e(TAG, "onAccessibilityEvent: "+event.getEventType());

        //AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
        AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
        containerCallMarketWithPrice(rootInfo);
       // containerCallMarket(rootInfo);
        //For market snip
        //marketClick(rootInfo);
        //for drop
        //dropClick(rootInfo);


    }
    public void containerCallMarket(AccessibilityNodeInfo rootInfo){
        List<AccessibilityNodeInfo> detail = rootInfo.findAccessibilityNodeInfosByText("Details");
        if (detail.size()>0){
            marketClick(rootInfo);
        }else{
            snipMarket(rootInfo);
        }
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
    /**
     * in buy list , rootinfo is Framlayout without resource-id
     * child0 is first chidl of rootinfo adn first viewgroup
     * viewGroup1 contain the scrollview with all list
     * @param rootInfo
     */
    public void snipMarket(AccessibilityNodeInfo rootInfo){
        List<AccessibilityNodeInfo> scrollViews = new ArrayList<>();
        findChildView(scrollViews,rootInfo);
        Log.e(TAG, "findChildView size: " +scrollViews.size() );
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

                    if ( mint != 0 && this.itemMarket != viewGroups){
                        this.itemMarket = viewGroups;
                        viewGroups.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        break;
                    }
                }


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
                    Thread.sleep(239500);
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
