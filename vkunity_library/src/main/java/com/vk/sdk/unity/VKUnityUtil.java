package com.vk.sdk.unity;

import com.unity3d.player.UnityPlayer;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sysop on 26.03.2016.
 */
public class VKUnityUtil {

    public static void sendMessageToUnity(String objName, String methodName, String msg) {
        UnityPlayer.UnitySendMessage(objName, methodName, msg);
    }

    public static String getErrorCodeName(VKError error) {
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

    public static JSONObject toJsonObject(VKError error) throws JSONException {
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

    public static JSONObject toJsonObject(VKRequest request) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("secure", request.secure);
        if (request.methodName != null)
            jsonObj.put("methodName", request.methodName);
        // ...
        return jsonObj;
    }

    public static JSONObject toJsonObject(VKAccessToken token) throws JSONException {
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

}
