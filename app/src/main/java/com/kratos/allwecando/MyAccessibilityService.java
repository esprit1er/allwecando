package com.kratos.allwecando;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";
    private static final String BUY_NOW = "BUY NOW";
    private AccessibilityNodeInfo mRootInfo;
    private long mDelayTime = 239800; // 4 minutes
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Map<String, AccessibilityNodeInfo> mCache = new HashMap<>();
    private Map<String, AccessibilityNodeInfo> mClickedNodes = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e(TAG, "onAccessibilityEvent: " + AccessibilityEvent.obtain(event.getEventType()));

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            mRootInfo = getRootInActiveWindow();

            if (mRootInfo != null) {
                List<AccessibilityNodeInfo> nodes = mRootInfo.findAccessibilityNodeInfosByText(BUY_NOW);
                for (AccessibilityNodeInfo node : nodes) {
                    String key = node.toString();
                    if (!mCache.containsKey(key) && !mClickedNodes.containsKey(key)) {
                        mCache.put(key, node);
                        performDropClick(node.getParent(), key);
                    }
                }
            }
        }
    }

    public void performDropClick(final AccessibilityNodeInfo nodeParent, final String key) {
        if (nodeParent == null) {
            return;
        }
        final AccessibilityNodeInfo clickableParent = findClickableParent(nodeParent);
        if (clickableParent == null || clickableParent.getChildCount() >= 3) {
            return;
        }
        clickableParent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        mClickedNodes.put(key, clickableParent);
        try {
            Thread.sleep(mDelayTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                clickableParent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                mCache.remove(key);
                mClickedNodes.remove(key);
                clickableParent.recycle();
            }
        }, mDelayTime);*/
    }

    private AccessibilityNodeInfo findClickableParent(AccessibilityNodeInfo node) {
        while (node != null && !node.isClickable()) {
            node = node.getParent();
        }
        return node;
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt: Something went wrong ");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout = 10;
        this.setServiceInfo(info);
        Log.d(TAG, "onServiceConnected: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCache.clear();
        mClickedNodes.clear();
    }

}

