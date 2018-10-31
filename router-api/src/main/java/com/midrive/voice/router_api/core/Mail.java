package com.midrive.voice.router_api.core;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import com.midrive.voice.router_annotation.model.RouteMeta;
import com.midrive.voice.router_api.template.Provider;
import com.midrive.voice.router_api.utils.Logger;

public class Mail extends RouteMeta {
    private Uri uri;
    private Object tag;             // A tag prepare for some thing wrong.
    private Bundle mBundle;         // Data to transform.
    private int flags = -1;         // Flags of route.
    private int timeout = 300;      // Navigation timeout, TimeUnit.Second.
    private Provider provider;     // It will be set value, if this postcard was provider.
    private String action;      //the action of intent.
    private boolean greenChannel;

    private static Mail head;   //the head of all mails, store in Mail's class.
    private Mail next;   //index of next mail.
    private static final int POOL = 5;
    private static int size = 0;
    private static final Object poolLock = new Object();


    public String getAction(){
        return action;
    }

    public Mail setAction(String action){
        this.action=action;
        return this;
    }

    public Uri getUri() {
        return uri;
    }

    public Mail setUri(Uri uri) {
        this.uri = uri;
        return this;
    }

    public Object getTag() {
        return tag;
    }

    public Mail setTag(Object tag) {
        this.tag = tag;
        return this;
    }

    public Bundle getExtras() {
        return mBundle;
    }

    public int getFlags() {
        return flags;
    }

    public Mail setFlags(int flags) {
        this.flags = flags;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public Mail setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public Provider getProvider() {
        return provider;
    }

    public Mail setProvider(Provider provider) {
        this.provider = provider;
        return this;
    }

    public boolean isGreenChannel() {
        return greenChannel;
    }

    public Mail greenChannel() {
        this.greenChannel = true;
        return this;
    }

    private Mail() {
        this(null, null);
    }

    private Mail(String path, String group) {
        this(path, group, null, null);
    }

    private Mail(String path, String group, Uri uri, Bundle bundle) {
        setPath(path);
        setGroup(group);
        this.uri = uri;
        this.mBundle = (null == bundle ? new Bundle() : bundle);
    }

    public static Mail obtainMail(String path, String group) {
        return obtainMail(path, group, null, null);
    }

    public static Mail obtainMail() {
        return obtainMail(null, null, null, null);
    }

    public static Mail obtainMail(String path, String group, Uri uri, Bundle bundle) {
        if(head == null) return new Mail(path, group, uri, bundle);
        else {
            synchronized (poolLock) {
                Mail mail = head;
                head = head.next;
                mail.next = null;
                size--;
                Logger.debug("",mail.toString());
                return mail;
            }
        }
    }

    /**
     * Navigation to the route with path in mail.
     * No param, will be use application context.
     */
    public Object navigation() {
        return navigation(null);
    }

    /**
     * Navigation to the route with path in mail.
     *
     * @param context Activity and so on.
     */
    public Object navigation(Context context) {
        return navigation(context, null);
    }

    /**
     * Navigation to the route with path in postcard.
     *
     * @param context Activity and so on.
     */
    public Object navigation(Context context, NavigationCallback callback) {
        return RouteBus.getInstance().navigation(context, this, -1, callback);
    }

    /**
     * Navigation to the route with path in postcard.
     *
     * @param mContext    Activity and so on.
     * @param requestCode startActivityForResult's param
     */
    public void navigation(Activity mContext, int requestCode) {
        navigation(mContext, requestCode, null);
    }

    /**
     * Navigation to the route with path in postcard.
     *
     * @param mContext    Activity and so on.
     * @param requestCode startActivityForResult's param
     */
    public void navigation(Activity mContext, int requestCode, NavigationCallback callback) {
        RouteBus.getInstance().navigation(mContext, this, requestCode, callback);
    }

    public void release() {
        this.clear();
        synchronized (poolLock) {
            if (size < POOL) {
                next = head;
                head = this;
                size++;
            }
        }
    }

    public void clear() {
        super.clear();
        uri = null;
        tag = null;
        mBundle = null;
        flags = -1;
        timeout = 300;
        provider = null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString())
          .append(", url="+uri)
          .append(", tag="+tag)
          .append(", bundle="+mBundle);

        return sb.toString();
    }

}
