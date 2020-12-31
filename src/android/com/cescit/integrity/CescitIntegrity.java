package com.cescit.integrity;

import android.app.Activity;
import android.content.Context;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class CescitIntegrity extends CordovaPlugin {

    private Activity activity;
    private Context context;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        activity = cordova.getActivity();
        context = activity.getApplicationContext();
        checkAndStopExecution();
        super.initialize(cordova, webView);
    }

    private void checkAndStopExecution() {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run () {
                try {
                    String ret = HttpUtil.getHttpRequestData((String) Config.getConfig("APK_HASH_URL"));
                    JSONObject obj = new JSONObject(ret);
                    if(obj.getBoolean("ApkIntegrity")) {
                        ApkIntegrity.check(context);
                    }
                    if(obj.getBoolean("ResIntegrity")) {
                        ResIntegrity.check(context);
                    }
                    if(obj.getBoolean("AssetsIntegrity")) {
                        AssetsIntegrity.check(context);
                    } 
                } catch (final Exception e) {
                    e.printStackTrace();
                    throw new TamperingException("Anti-Tampering check failed");
                }
            }
        });
    }

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

        if ("verify".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run () {
                    PluginResult result;
                    try {
                        // DebugDetection.check(activity.getPackageName());
                        JSONObject response = new JSONObject();
                        response.put("assets", AssetsIntegrity.check(context));
                        response.put("res", ResIntegrity.check(context));
                        response.put("apk", ApkIntegrity.check(context));
                        result = new PluginResult(PluginResult.Status.OK, response);
                    } catch (Exception e) {
                        result = new PluginResult(PluginResult.Status.ERROR, e.toString());
                    }
                    callbackContext.sendPluginResult(result);
                }
            });
            return true;
        }

        if ("getList".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run () {
                    PluginResult result;
                    try {
                        JSONObject response = new JSONObject();
                        response.put("assets", AssetsIntegrity.getHashString(context));
                        response.put("res", ResIntegrity.getHashString(context));
                        response.put("apk", ApkIntegrity.getHashString(context));
                        result = new PluginResult(PluginResult.Status.OK, response);
                    } catch (Exception e) {
                        result = new PluginResult(PluginResult.Status.ERROR, e.toString());
                    }
                    callbackContext.sendPluginResult(result);
                }
            });
            return true;
        }

        return false;

    }

}
