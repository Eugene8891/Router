package com.midrive.voice.router_api.template;

import java.util.Map;

/**
 * Template of interceptor group.
 *
 */
public interface InterceptorGroup {
    /**
     * Load interceptor to input
     *
     * @param interceptor input
     */
    void load(Map<Integer, Class<? extends Interceptor>> interceptor);
}
