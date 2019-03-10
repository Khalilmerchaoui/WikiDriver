package com.app.wikidriver.services;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "tagged";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG, "ACC::onAccessibilityEvent: " + event.getEventType());

        //TYPE_WINDOW_STATE_CHANGED == 32
        if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event
                .getEventType()) {
            final AccessibilityNodeInfo nodeInfo = event.getSource();
            Log.i(TAG, "ACC::onAccessibilityEvent: nodeInfo=" + nodeInfo);
            if (nodeInfo == null) {
                return;
            }

            boolean appStopped = false;
            List<AccessibilityNodeInfo> list = nodeInfo
                    .findAccessibilityNodeInfosByText("FORCER L'ARRÊT");

            for (AccessibilityNodeInfo node : list) {
                Log.i(TAG, "ACC::onAccessibilityEvent: left_button " + node);
                //if(!node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                appStopped = true;
            }

            List<AccessibilityNodeInfo> list1 = nodeInfo
                    .findAccessibilityNodeInfosByText("OK");
            for (AccessibilityNodeInfo node : list1) {
                Log.i(TAG, "ACC::onAccessibilityEvent: button1 " + node);
                //if(!node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            if(appStopped)
                performGlobalAction(GLOBAL_ACTION_BACK);
        }

    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "ACC::onServiceConnected: ");
    }

    @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub
        Log.i(TAG, "ACC::onInterrupt: ");
    }
}
