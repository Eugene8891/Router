package com.midrive.voice.router_annotation.model;

import com.midrive.voice.router_annotation.annotation.Route;
import java.util.Map;
import javax.lang.model.element.Element;

/**
 * It contains basic route information.
 *
 */
public class RouteMeta {
    private RouteType type;         // Type of route
    private Element rawType;        // Raw type of route
    private Class<?> destination;   // Destination
    private String path;            // Path of route
    private String group;           // Group of route
    private int priority = -1;      // The smaller the number, the higher the priority
    private int extra;              // Extra data
    private String name;

    public RouteMeta() {
    }

    public RouteType getType() {
        return type;
    }

    public void setType(RouteType type) {
        this.type = type;
    }

    public Element getRawType() {
        return rawType;
    }

    public void setRawType(Element rawType) {
        this.rawType = rawType;
    }

    public Class<?> getDestination() {
        return destination;
    }

    public void setDestination(Class<?> destination) {
        this.destination = destination;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getExtra() {
        return extra;
    }

    public void setExtra(int extra) {
        this.extra = extra;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param destination destination
     * @param path        path
     * @param group       group
     * @param priority    priority
     * @param extra       extra
     * @return this
     */
    public static RouteMeta build(RouteType type, Class<?> destination, String path, String group, int priority, int extra) {
        return new RouteMeta(type,null, destination, null, path, group, null, priority, extra);
    }

    /**
     *
     * @param destination destination
     * @param path        path
     * @param group       group
     * @param paramsType  paramsType
     * @param priority    priority
     * @param extra       extra
     * @return this
     */
    public static RouteMeta build(RouteType type, Class<?> destination, String path, String group, Map<String, Integer> paramsType, int priority, int extra) {
        return new RouteMeta(type,null, destination, null, path, group, paramsType, priority, extra);
    }

    /**
     * Type
     *
     * @param route       route
     * @param destination destination
     */
    public RouteMeta(Route route, Class<?> destination, RouteType type) {
        this(type,null, destination, route.name(), route.path(), route.group(), null, route.priority(), 0);
    }

    /**
     * Type
     *
     * @param route      route
     * @param rawType    rawType
     * @param paramsType paramsType
     */
    public RouteMeta(Route route, Element rawType, RouteType type, Map<String, Integer> paramsType) {
        this(type, rawType, null, route.name(), route.path(), route.group(), paramsType, route.priority(), 0);
    }

    /**
     * Type
     *
     * @param rawType     rawType
     * @param destination destination
     * @param path        path
     * @param group       group
     * @param paramsType  paramsType
     * @param priority    priority
     * @param extra       extra
     */
    public RouteMeta(RouteType type, Element rawType, Class<?> destination, String name, String path, String group, Map<String, Integer> paramsType, int priority, int extra) {
        this.type = type;
        this.name = name;
        this.destination = destination;
        this.rawType = rawType;
        this.path = path;
        this.group = group;
        this.priority = priority;
        this.extra = extra;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RouteMeta{")
          .append(", rawType=" + rawType)
          .append(", destination=" + destination)
          .append(", path='" + path)
          .append(", group='" + group)
          .append(", name='" + name);
        return sb.toString();
    }

    public void clear() {
        type = null;
        rawType = null;
        destination = null;
        path = null;
        group = null;
        priority = -1;
        extra = 0;
        name = null;
    }
}