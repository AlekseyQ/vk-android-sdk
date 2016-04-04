package com.vk.sdk.unity;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
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

    class VKUsersGetRequestListenerImpl extends VKRequest.VKRequestListener {

        private String completeObjName;
        private String completeMethodName;
        private String errorObjName;
        private String errorMethodName;
        private String attemptFailedObjName;
        private String attemptFailedMethodName;

        VKUsersGetRequestListenerImpl(String completeObjName, String completeMethodName,
                                             String errorObjName, String errorMethodName,
                                             String attemptFailedObjName, String attemptFailedMethodName) {
            this.completeObjName = completeObjName;
            this.completeMethodName = completeMethodName;
            this.errorObjName = errorObjName;
            this.errorMethodName = errorMethodName;
            this.attemptFailedObjName = attemptFailedObjName;
            this.attemptFailedMethodName = attemptFailedMethodName;
        }

        private void sendMessageToUnity(String objName, String methodName, String msg) {
            VKUnityUtil.sendMessageToUnity(objName, methodName, msg);
        }

        private void sendComplete(String msg) {
            sendMessageToUnity(completeObjName, completeMethodName, msg);
        }

        private void sendError(String msg) {
            sendMessageToUnity(errorObjName, errorMethodName, msg);
        }

        private void sendAttemptFailed(String msg) {
            sendMessageToUnity(attemptFailedObjName, attemptFailedMethodName, msg);
        }

        @Override
        public void onComplete(VKResponse response) {
            if(response == null) {
                sendComplete(VKUnity.RESULT_NULL);
                return;
            }

            try {
                JSONObject jsonObj = VKUnityUtil.toJsonObject(response);
                sendComplete(jsonObj.toString());
            } catch (JSONException e) {
                sendComplete(VKUnity.RESULT_MAKE_ERROR);
                if (VKSdk.DEBUG)
                    e.printStackTrace();
            }
        }

        @Override
        public void onError(VKError error) {
            if(error == null) {
                sendError(VKUnity.RESULT_NULL);
                return;
            }

            try {
                JSONObject jsonObj = VKUnityUtil.toJsonObject(error);
                sendError(jsonObj.toString());
            } catch (JSONException e) {
                sendError(VKUnity.RESULT_MAKE_ERROR);
                if (VKSdk.DEBUG)
                    e.printStackTrace();
            }
        }

        @Override
        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
            //I don't really believe in progress
            if(request == null) {
                sendAttemptFailed(VKUnity.RESULT_NULL);
                return;
            }

            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("request", VKUnityUtil.toJsonObject(request));
                jsonObj.put("attemptNumber", attemptNumber);
                jsonObj.put("totalAttempts", totalAttempts);
                sendAttemptFailed(jsonObj.toString());
            } catch (JSONException e) {
                sendAttemptFailed(VKUnity.RESULT_MAKE_ERROR);
                if (VKSdk.DEBUG)
                    e.printStackTrace();
            }
        }
    }

    public void doUsersGetRequest(Activity activity,
                                  String completeObjName, String completeMethodName,
                                  String errorObjName, String errorMethodName,
                                  String attemptFailedObjName, String attemptFailedMethodName,
                                  String... args) {
        VKRequest request = VKApi.users().get(VKParameters.from(args));
        request.executeWithListener(new VKUsersGetRequestListenerImpl(completeObjName, completeMethodName, errorObjName, errorMethodName, attemptFailedObjName, attemptFailedMethodName));
    }
}
