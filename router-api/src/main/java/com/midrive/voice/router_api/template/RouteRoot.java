package com.midrive.voice.router_api.template;

import java.util.Map;

/**
 * Root element.
 *
 */
public interface RouteRoot {

    /**
     * Load routes to input
     * @param groups input
     */
    void load(Map<String, Class<? extends RouteGroup>> groups);
}
