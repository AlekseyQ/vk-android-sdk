package com.vk.sdk.unity;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sysop on 26.03.2016.
 */
public class VKLoginCallbackImpl implements VKCallback<VKAccessToken> {
    private String doneCallbackObjName;
    private String doneCallbackMethodName;

    private String failCallbackObjName;
    private String failCallbackMethodName;

    public VKLoginCallbackImpl(String doneObjName, String doneMethodName, String failObjName, String failMethodName) {
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

    interface JsonBuilder<T> {
        JSONObject buildJson(T res) throws JSONException;
    }

    interface MessageSender {
        void sendMessage(String msg);
    }

    private <T> void onJsonCallbackDefaultHandler(T res, MessageSender msgSender, JsonBuilder<T> jsonBuilder) {
        if(res == null) {
            msgSender.sendMessage(VKUnity.RESULT_NULL);
            return;
        }

        try {
            JSONObject jsonObj = jsonBuilder.buildJson(res);
            msgSender.sendMessage(jsonObj.toString());
        } catch (JSONException e) {
            msgSender.sendMessage(VKUnity.RESULT_MAKE_ERROR);
            if (VKSdk.DEBUG)
                e.printStackTrace();
        }
    }

    @Override
    public void onResult(VKAccessToken res) {
        onJsonCallbackDefaultHandler(res, new MessageSender() {
            @Override
            public void sendMessage(String msg) {
                sendDone(msg);
            }
        }, new JsonBuilder<VKAccessToken>() {
            @Override
            public JSONObject buildJson(VKAccessToken res) throws JSONException {
                return VKUnityUtil.toJsonObject(res);
            }
        });
    }

    @Override
    public void onError(VKError error) {
        onJsonCallbackDefaultHandler(error, new MessageSender() {
            @Override
            public void sendMessage(String msg) {
                sendFail(msg);
            }
        }, new JsonBuilder<VKError>() {
            @Override
            public JSONObject buildJson(VKError res) throws JSONException {
                return VKUnityUtil.toJsonObject(res);
            }
        });
    }
}
