package com.midrive.voice.router_api.template;

import com.midrive.voice.router_annotation.model.RouteMeta;

import java.util.Map;

/**
 * Group route element.
 *
 */
public interface RouteGroup {
    /**
     * Fill the routes with routes in group.
     * @param routes input
     */
    void load(Map<String, RouteMeta> routes);
}
