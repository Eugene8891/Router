package com.midrive.voice.router_api.template;

import com.midrive.voice.router_annotation.model.RouteMeta;

import java.util.Map;

/**
 * Template of provider group.
 *
 */
public interface ProviderGroup {
    /**
     * Load providers map to input
     *
     * @param providers input
     */
    void load(Map<String, RouteMeta> providers);
}