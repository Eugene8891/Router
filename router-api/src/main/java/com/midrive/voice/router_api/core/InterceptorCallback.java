package com.midrive.voice.router_api.core;


/**
 * The callback of interceptor.
 *
 */
public interface InterceptorCallback {

    /**
     * Continue process
     *
     * @param mail route meta
     */
    void onContinue(Mail mail);

    /**
     * Interrupt process, pipeline will be destory when this method called.
     *
     * @param exception Reason of interrupt.
     */
    void onInterrupt(Throwable exception);
}
