package com.vk.sdk.unity;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdk.LoginState;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.util.VKStringJoiner;
import com.vk.sdk.util.VKUtil;
import com.unity3d.player.UnityPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by sysop on 22.03.2016.
 */
public class VKUnity {
    private static final String RESULT_MAKE_ERROR = "result_failure";

    private Context context;
    private VKSdk vkSdk;

    private String unityTokenTrackerObjName;
    private String unityTokenTrackerMethodName;

    /**
     * Scope is set of required permissions for your application
     *
     * @see <a href="https://vk.com/dev/permissions">vk.com api permissions documentation</a>
     */
    private final List<String> requestedPermissions = new ArrayList<>();

    private final VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if(unityTokenTrackerObjName != null && unityTokenTrackerMethodName != null) {
                JSONObject jsonObj = new JSONObject();
                try {
                    if(oldToken != null) {
                        jsonObj.put("oldToken", toJsonObject(oldToken));
                    }
                    if(newToken != null) {
                        jsonObj.put("newToken", toJsonObject(newToken));
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
        UnityPlayer.UnitySendMessage(objName, methodName, msg);
    }

    class VKCallbackImpl implements VKCallback<LoginState> {
        private String doneCallbackObjName;
        private String doneCallbackMethodName;

        private String failCallbackObjName;
        private String failCallbackMethodName;

        public VKCallbackImpl(String doneObjName, String doneMethodName, String failObjName, String failMethodName) {
            this.doneCallbackObjName = doneObjName;
            this.doneCallbackMethodName = doneMethodName;
            this.failCallbackObjName = failObjName;
            this.failCallbackMethodName = failMethodName;
        }

        private void sendDone(String msg) {
            sendMessageToUnity(doneCallbackObjName, doneCallbackMethodName, msg);
        }

        private void sendFail(String msg) {
            sendMessageToUnity(failCallbackObjName, failCallbackMethodName, msg);
        }

        @Override
        public void onResult(LoginState res) {
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("loginState", res.toString());
                sendDone(jsonObj.toString());
            } catch (JSONException e) {
                sendDone(RESULT_MAKE_ERROR);
                if (VKSdk.DEBUG)
                    e.printStackTrace();
            }
        }

        private String getErrorCodeName(VKError error) {
            switch (error.errorCode) {
                case VKError.VK_API_ERROR:
                    return "VK_API_ERROR";
                case VKError.VK_CANCELED:
                    return "VK_CANCELED";
                case VKError.VK_REQUEST_NOT_PREPARED:
                    return "VK_REQUEST_NOT_PREPARED";
                case VKError.VK_JSON_FAILED:
                    return "VK_JSON_FAILED";
                case VKError.VK_REQUEST_HTTP_FAILED:
                    return "VK_REQUEST_HTTP_FAILED";
                default:
                    return "VK_OTHER_ERROR";
            }
        }

        private JSONObject toJsonObject(VKError error) throws JSONException {
            JSONObject jsonObj = new JSONObject();
            if (error.httpError != null)
                jsonObj.putOpt("httpError", error.httpError.getMessage());
            if (error.apiError != null)
                jsonObj.put("apiError", toJsonObject(error.apiError));
            if (error.request != null)
                jsonObj.put("request", toJsonObject(error.request));
            jsonObj.put("errorCode", error.errorCode);
            jsonObj.put("errorCodeName", getErrorCodeName(error));
            jsonObj.putOpt("errorMessage", error.errorMessage);
            jsonObj.putOpt("errorReason", error.errorReason);
            jsonObj.putOpt("captchaSid", error.captchaSid);
            jsonObj.putOpt("captchaImg", error.captchaImg);
            jsonObj.putOpt("redirectUri", error.redirectUri);

            return jsonObj;
        }

        private JSONObject toJsonObject(VKRequest request) throws JSONException {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("secure", request.secure);
            if (request.methodName != null)
                jsonObj.put("methodName", request.methodName);
            // ...
            return jsonObj;
        }

        @Override
        public void onError(VKError error) {
            try {
                JSONObject jsonObj = toJsonObject(error);
                sendFail(jsonObj.toString());
            } catch (JSONException e) {
                sendFail(RESULT_MAKE_ERROR);
                if (VKSdk.DEBUG)
                    e.printStackTrace();
            }
        }
    }

    public void init(Activity activity, int appId, String doneObjName, String doneMethodName, String failObjName, String failMethodName) {
        VKCallbackImpl loginStateCallback = new VKCallbackImpl(doneObjName, doneMethodName, failObjName, failMethodName);
        vkAccessTokenTracker.startTracking();
        vkSdk = VKSdk.customInitialize(context, appId, null, loginStateCallback);
    }

    public boolean isLoggedIn() {
        return VKSdk.isLoggedIn();
    }

    private JSONObject toJsonObject(VKAccessToken token) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.putOpt("accessToken", token.accessToken);
        jsonObj.put("expiresIn", token.expiresIn);
        jsonObj.putOpt("userId", token.userId);
        jsonObj.putOpt("secret", token.secret);
        jsonObj.put("httpsRequired", token.httpsRequired);
        jsonObj.put("created", token.created);
        jsonObj.putOpt("email", token.email);

        JSONArray jsonScopeObj = new JSONArray();
        for (String scope : token.scope.keySet()) {
            if(token.scope.get(scope)) {
                jsonScopeObj.put(scope);
            }
        }
        jsonObj.put("scope", jsonScopeObj);

        return jsonObj;
    }

    public String getVKAccessToken() {
        VKAccessToken token = VKAccessToken.currentToken();
        if(token == null) {
            return "";
        }

        try {
            JSONObject jsonObj = toJsonObject(token);
            return jsonObj.toString();
            //return new Gson().toJson(token);
        } catch (JSONException e) {
            if (VKSdk.DEBUG)
                e.printStackTrace();

            return RESULT_MAKE_ERROR;
        }
    }

    public void login(Activity activity, String doneObjName, String doneMethodName, String failObjName, String failMethodName) {
        if(!VKSdk.isLoggedIn()) {
            String[] sMyScope = new String[requestedPermissions.size()];
            requestedPermissions.toArray(sMyScope);
            VKSdk.login(activity, sMyScope);
        }
    }

    public void logout(Activity activity) {
        if(VKSdk.isLoggedIn()) {
            VKSdk.logout();
        }
    }

}
