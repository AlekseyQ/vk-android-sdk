package com.vk.sdk.unity;

import android.app.Activity;
import android.content.Context;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;
import com.vk.sdk.util.VKUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by sysop on 22.03.2016.
 */
public class VKUnity {
    public static final String RESULT_MAKE_ERROR = "result_failure";
    public static final String RESULT_NULL = "";

    private Context context;
    private VKSdk vkSdk;

    private String unityTokenTrackerObjName;
    private String unityTokenTrackerMethodName;

    /**
     * Scope is set of required permissions for your application
     *
     * @see <a href="https://vk.com/dev/permissions">vk.com api permissions documentation</a>
     */
    private final ArrayList<String> requestedPermissions = new ArrayList<>();

    private final VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if(unityTokenTrackerObjName != null && unityTokenTrackerMethodName != null) {
                JSONObject jsonObj = new JSONObject();
                try {
                    if(oldToken != null) {
                        jsonObj.put("oldToken", VKUnityUtil.toJsonObject(oldToken));
                    }
                    if(newToken != null) {
                        jsonObj.put("newToken", VKUnityUtil.toJsonObject(newToken));
                    }

                    sendMessageToUnity(unityTokenTrackerObjName, unityTokenTrackerMethodName, jsonObj.toString());
                } catch (JSONException e) {
                    sendMessageToUnity(unityTokenTrackerObjName, unityTokenTrackerMethodName, RESULT_MAKE_ERROR);
                    if (VKSdk.DEBUG)
                        e.printStackTrace();
                }

            }
        }
    };

    public VKUnity(Context context) {
        this.context = context;
    }

    public void clearPermissions() {
        requestedPermissions.clear();
    }

    public void addPermission(String permission) {
        requestedPermissions.add(permission);
    }

    public void registerAccessTokenTracker(String objName, String methodName) {
        unityTokenTrackerObjName = objName;
        unityTokenTrackerMethodName = methodName;
    }

    public void unregisterAccessTokenTracker() {
        unityTokenTrackerObjName = null;
        unityTokenTrackerMethodName = null;
    }

    public String[] getCertificateFingerprint(Activity activity) {
        String[] fingerprints = VKUtil.getCertificateFingerprint(activity, activity.getPackageName());
        return fingerprints;
    }

    public String getPackageName(Activity activity) {
        return activity.getPackageName();
    }

    private void sendMessageToUnity(String objName, String methodName, String msg) {
        VKUnityUtil.sendMessageToUnity(objName, methodName, msg);
    }

    public void init(Activity activity, int appId, String doneObjName, String doneMethodName, String failObjName, String failMethodName) {
        VKInitCallbackImpl loginStateCallback = new VKInitCallbackImpl(doneObjName, doneMethodName, failObjName, failMethodName);
        vkAccessTokenTracker.startTracking();
        vkSdk = VKSdk.customInitialize(context, appId, null, loginStateCallback);
    }

    public boolean isLoggedIn() {
        return VKSdk.isLoggedIn();
    }

    public String getVKAccessToken() {
        VKAccessToken token = VKAccessToken.currentToken();
        if(token == null) {
            return RESULT_NULL;
        }

        try {
            JSONObject jsonObj = VKUnityUtil.toJsonObject(token);
            return jsonObj.toString();
            //return new Gson().toJson(token);
        } catch (JSONException e) {
            if (VKSdk.DEBUG)
                e.printStackTrace();

            return RESULT_MAKE_ERROR;
        }
    }

    public boolean login(Activity activity, String doneObjName, String doneMethodName, String failObjName, String failMethodName) {
        if(VKSdk.isLoggedIn()) {
            return false;
        }

        VKUnityLoginActivity.startActivity(activity, requestedPermissions, doneObjName, doneMethodName, failObjName, failMethodName);
        return true;
    }

    public boolean logout(Activity activity) {
        if(!VKSdk.isLoggedIn()) {
            return false;
        }
        VKSdk.logout();
        return true;
    }

}
