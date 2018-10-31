package com.midrive.voice.router_api.service;

import com.midrive.voice.router_api.core.InterceptorCallback;
import com.midrive.voice.router_api.core.Mail;
import com.midrive.voice.router_api.template.Provider;

/**
 * Intercept service
 *
 */
public interface InterceptorService extends Provider {

    /**
     * Do interceptions
     */
    void doInterceptions(final Mail mail, final InterceptorCallback callback);
}
