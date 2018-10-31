package com.midrive.voice.router_api.template;

import com.midrive.voice.router_api.core.InterceptorCallback;
import com.midrive.voice.router_api.core.Mail;

/**
 * Used for inject custom logic when navigation.
 *
 */
public interface Interceptor extends Provider {

    /**
     * The operation of this interceptor.
     *
     * @param mail meta
     * @param callback cb
     */
    void process(Mail mail, InterceptorCallback callback);
}
