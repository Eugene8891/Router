package com.midrive.router.test_router;

import android.content.Context;

import com.midrive.voice.router_annotation.annotation.Intercept;
import com.midrive.voice.router_api.core.InterceptorCallback;
import com.midrive.voice.router_api.core.Mail;
import com.midrive.voice.router_api.template.Interceptor;

@Intercept(priority = 2)
public class TestInterceptor implements Interceptor {
    @Override
    public void process(Mail mail, InterceptorCallback callback) {

    }

    @Override
    public void init(Context context) {

    }
}
