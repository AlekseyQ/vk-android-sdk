package com.vk.sdk.unity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKServiceActivity;
import com.vk.sdk.api.VKError;

import java.util.ArrayList;

public class VKUnityLoginActivity extends Activity {

    private static final String KEY_SCOPE_LIST = "arg1";
    private static final String KEY_DONE_OBJECT_NAME = "arg2";
    private static final String KEY_DONE_METHOD_NAME = "arg3";
    private static final String KEY_FAIL_OBJECT_NAME = "arg4";
    private static final String KEY_FAIL_METHOD_NAME = "arg5";

    private String doneObjName;
    private String doneMethodName;
    private String failObjName;
    private String failMethodName;

    public static void startActivity(@NonNull Activity act, @NonNull ArrayList<String> scopeList, @NonNull String doneObjName, @NonNull String doneMethodName, @NonNull String failObjName, @NonNull String failMethodName) {
        Context appCtx = act.getApplicationContext();
        Intent intent = new Intent(appCtx, VKUnityLoginActivity.class);
        intent.putStringArrayListExtra(KEY_SCOPE_LIST, scopeList);
        intent.putExtra(KEY_DONE_OBJECT_NAME, doneObjName);
        intent.putExtra(KEY_DONE_METHOD_NAME, doneMethodName);
        intent.putExtra(KEY_FAIL_OBJECT_NAME, failObjName);
        intent.putExtra(KEY_FAIL_METHOD_NAME, failMethodName);
        act.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        ArrayList<String> scopeList = intent.getStringArrayListExtra(KEY_SCOPE_LIST);
        doneObjName = intent.getStringExtra(KEY_DONE_OBJECT_NAME);
        doneMethodName = intent.getStringExtra(KEY_DONE_METHOD_NAME);
        failObjName = intent.getStringExtra(KEY_FAIL_OBJECT_NAME);
        failMethodName = intent.getStringExtra(KEY_FAIL_METHOD_NAME);

        String[] sMyScope = new String[scopeList.size()];
        scopeList.toArray(sMyScope);
        VKSdk.login(this, sMyScope);
    }

    @Override
    protected void onDestroy() {
        //TODO Возможно, что в onCreate не апуститься активити vk или пользователь нажмет на return
        //TODO кнопку и VKUnityLoginActivity закроется. Нужно проверить вызывается ли onActivityResult при этом

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKLoginCallbackImpl loginStateCallback = new VKLoginCallbackImpl(doneObjName, doneMethodName, failObjName, failMethodName);
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, loginStateCallback)) {
            // Неизвестная ситуация. Отправляем пустую ошибку.
            loginStateCallback.onError(null);
        }
        finish();
    }
}
