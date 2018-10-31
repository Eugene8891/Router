package com.midrive.voice.router_api.core;

/**
 * Callback after navigation.
 *
 */
public interface NavigationCallback {

    /**
     * Callback when find the destination.
     *
     * @param mail meta
     */
    void onFound(Mail mail);

    /**
     * Callback after lose your way.
     *
     * @param mail meta
     */
    void onLost(Mail mail);

    /**
     * Callback after navigation.
     *
     * @param mail meta
     */
    void onArrival(Mail mail);

    /**
     * Callback on interrupt.
     *
     * @param mail meta
     */
    void onInterrupt(Mail mail);
}
