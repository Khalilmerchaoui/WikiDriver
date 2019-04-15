package app.m26.wikidriver.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "litan";
    private boolean isKilled = false;
    private String deviceLanguage;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isKilled = false;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        deviceLanguage = Locale.getDefault().getLanguage();
        String builder = "";
        String second = "";
        AccessibilityNodeInfo nody = event.getSource();
        second += nody + "";
        if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.getEventType()) {
            String forceString = deviceLanguage.equals("fr") ? "FORCER L'ARRÊT" : "FORCE STOP";
            Log.i("languagetag", forceString);

            AccessibilityNodeInfo nodeInfo = event.getSource();
            Log.i(TAG, "ACC::onAccessibilityEvent: nodeInfo=" + nodeInfo);
            builder += "ACC::onAccessibilityEvent: nodeInfo=" + nodeInfo;
            if (nodeInfo == null) {
                return;
            }
            isKilled = false;
            List<AccessibilityNodeInfo> list = new ArrayList<>();
            if ("com.android.settings.applications.InstalledAppDetailsTop".equals(event.getClassName())) {
                if (Build.VERSION.SDK_INT >= 18) {
                    list = nodeInfo.findAccessibilityNodeInfosByText(forceString);

                } else if (Build.VERSION.SDK_INT >= 14) {
                    list = nodeInfo.findAccessibilityNodeInfosByText(forceString);
                }
                for (AccessibilityNodeInfo node : list) {
                    Log.i(TAG, "ACC::onAccessibilityEvent: right_button " + node);
                    builder += "ACC::onAccessibilityEvent: right_button " + node;
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    isKilled = true;
                }
            } else if ("android.app.AlertDialog".equals(event.getClassName())) {
                list = new ArrayList<>();
                if (Build.VERSION.SDK_INT >= 18) {
                    list = nodeInfo.findAccessibilityNodeInfosByViewId("android:id/button1");

                } else if (Build.VERSION.SDK_INT >= 14) {
                    list = nodeInfo.findAccessibilityNodeInfosByText("android:id/button1");
                }

                for (final AccessibilityNodeInfo node : list) {
                    Log.i(TAG, "ACC::onAccessibilityEvent: button1 " + node);
                    builder += "ACC::onAccessibilityEvent: button1 " + node;
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    isKilled = true;
                    //node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
            if(isKilled) {
                performGlobalAction(GLOBAL_ACTION_BACK);
                /*Toast.makeText(this, builder, Toast.LENGTH_SHORT).show();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", builder);
                clipboard.setPrimaryClip(clip);*/
            }

        }
    }


    @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub
        Log.i("Interrupt", "Interrupt");
    }

    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo info = getServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOWS_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;

        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        // We are keeping the timeout to 0 as we don’t need any delay or to pause our accessibility events
        info.feedbackType = AccessibilityEvent.TYPES_ALL_MASK;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);
        // Toast.makeText(getApplicationContext(), "onServiceConnected", Toast.LENGTH_SHORT).show();
    }

    private static void logd(String msg) {
        Log.d(TAG, msg);
    }

    private static void logw(String msg) {
        Log.w(TAG, msg);
    }

    private static void logi(String msg) {
        Log.i(TAG, msg);
    }

}
