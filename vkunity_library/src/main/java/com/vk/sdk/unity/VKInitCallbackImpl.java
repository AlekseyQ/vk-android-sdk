package com.vk.sdk.unity;

import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class VKInitCallbackImpl implements VKCallback<VKSdk.LoginState> {
    private String doneCallbackObjName;
    private String doneCallbackMethodName;

    private String failCallbackObjName;
    private String failCallbackMethodName;

    public VKInitCallbackImpl(String doneObjName, String doneMethodName, String failObjName, String failMethodName) {
        this.doneCallbackObjName = doneObjName;
        this.doneCallbackMethodName = doneMethodName;
        this.failCallbackObjName = failObjName;
        this.failCallbackMethodName = failMethodName;
    }

    private void sendMessageToUnity(String objName, String methodName, String msg) {
        VKUnityUtil.sendMessageToUnity(objName, methodName, msg);
    }

    private void sendDone(String msg) {
        sendMessageToUnity(doneCallbackObjName, doneCallbackMethodName, msg);
    }

    private void sendFail(String msg) {
        sendMessageToUnity(failCallbackObjName, failCallbackMethodName, msg);
    }


    @Override
    public void onResult(VKSdk.LoginState res) {
        if(res == null) {
            sendFail(VKUnity.RESULT_NULL);
            return;
        }

        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("loginState", res.toString());
            sendDone(jsonObj.toString());
        } catch (JSONException e) {
            sendDone(VKUnity.RESULT_MAKE_ERROR);
            if (VKSdk.DEBUG)
                e.printStackTrace();
        }
    }

    @Override
    public void onError(VKError error) {
        if(error == null) {
            sendFail(VKUnity.RESULT_NULL);
            return;
        }

        try {
            JSONObject jsonObj = VKUnityUtil.toJsonObject(error);
            sendFail(jsonObj.toString());
        } catch (JSONException e) {
            sendFail(VKUnity.RESULT_MAKE_ERROR);
            if (VKSdk.DEBUG)
                e.printStackTrace();
        }
    }
}
