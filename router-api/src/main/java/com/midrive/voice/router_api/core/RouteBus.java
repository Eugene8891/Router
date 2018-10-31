package com.midrive.voice.router_api.core;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import com.midrive.voice.router_annotation.model.RouteMeta;
import com.midrive.voice.router_api.service.InterceptorService;
import com.midrive.voice.router_api.template.Interceptor;
import com.midrive.voice.router_api.template.InterceptorGroup;
import com.midrive.voice.router_api.template.Provider;
import com.midrive.voice.router_api.template.ProviderGroup;
import com.midrive.voice.router_api.template.RouteGroup;
import com.midrive.voice.router_api.template.RouteRoot;
import com.midrive.voice.router_api.thread.DefaultPoolExecutor;
import com.midrive.voice.router_api.utils.ClassUtils;
import com.midrive.voice.router_api.utils.Logger;
import com.midrive.voice.router_api.utils.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;
import static com.midrive.voice.router_api.utils.Const.ROUTE_ROOT_PAKCAGE;
import static com.midrive.voice.router_api.utils.Const.SDK_NAME;
import static com.midrive.voice.router_api.utils.Const.SEPARATOR;
import static com.midrive.voice.router_api.utils.Const.SUFFIX_INTERCEPTORS;
import static com.midrive.voice.router_api.utils.Const.SUFFIX_PROVIDERS;
import static com.midrive.voice.router_api.utils.Const.SUFFIX_ROOT;

/**
 * Dispatch route messages.
 */
public class RouteBus {
    private static Context mContext;
    private static ThreadPoolExecutor executor = DefaultPoolExecutor.getInstance();
    private static RouteBus instance;

    //=== warehouse ===
    private static Map<String, Class<? extends RouteGroup>> groupsIndex = new HashMap<>();
    private static Map<String, RouteMeta> routes = new HashMap<>();

    // Cache provider
    private static Map<Class, Provider> providers = new HashMap<>();
    private static Map<String, RouteMeta> providersIndex = new HashMap<>();

    // Cache interceptor
    private static Map<Integer, Class<? extends Interceptor>> interceptorsIndex = new UniqueKeyTreeMap<>("More than one interceptor use same priority [%s]");
    private static List<Interceptor> interceptors = new ArrayList<>();

    private static Handler mHandler;
    private static InterceptorService interceptorService;

    public static RouteBus getInstance() {
        if(instance == null) {
            instance = new RouteBus();
        }
        return instance;
    }

    public static void init(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        try {
            long startInit = System.currentTimeMillis();
            Set<String> routerMap = ClassUtils.getFileNameByPackageName(mContext, ROUTE_ROOT_PAKCAGE);

            for (String className : routerMap) {
                if (className.startsWith(ROUTE_ROOT_PAKCAGE + "." + SDK_NAME + SEPARATOR + SUFFIX_ROOT)) {
                    // This one of root elements, load root.
                    ((RouteRoot) (Class.forName(className).getConstructor().newInstance())).load(groupsIndex);
                } else if (className.startsWith(ROUTE_ROOT_PAKCAGE + "." + SDK_NAME + SEPARATOR + SUFFIX_INTERCEPTORS)) {
                    // Load interceptorMeta
                    ((InterceptorGroup) (Class.forName(className).getConstructor().newInstance())).load(interceptorsIndex);
                } else if (className.startsWith(ROUTE_ROOT_PAKCAGE + "." + SDK_NAME + SEPARATOR + SUFFIX_PROVIDERS)) {
                    // Load providerIndex
                    ((ProviderGroup) (Class.forName(className).getConstructor().newInstance())).load(providersIndex);
                }
            }
        } catch(Exception e) {

        }
        //init interceptor
        interceptorService = (InterceptorService) getInstance().build("router/service/interceptor").navigation();
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public Map<Integer, Class<? extends Interceptor>> getInterceptorsIndex() {
        return interceptorsIndex;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    /**
     * Build postcard by path and default group
     */
    protected Mail build(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new RuntimeException("build path Parameter is invalid!");
        } else {
            return build(path, extractGroup(path));
        }
    }

    /**
     * Build postcard by uri
     */
    protected Mail build(Uri uri) {
        if (null == uri || TextUtils.isEmpty(uri.toString())) {
            throw new RuntimeException("build uri Parameter invalid!");
        } else {
            return Mail.obtainMail(uri.getPath(), extractGroup(uri.getPath()), uri, null);
        }
    }

    /**
     * Build postcard by path and group
     */
    protected Mail build(String path, String group) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(group)) {
            throw new RuntimeException("Parameter is invalid!");
        } else {
            return Mail.obtainMail(path, group);
        }
    }

    /**
     * Extract the default group from path.
     */
    private String extractGroup(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new RuntimeException("Extract the default group failed, the path must be start with '/' and contain more than 2 '/'!");
        }
        try {
            String defaultGroup = path.substring(1, path.indexOf("/", 1));
            if (TextUtils.isEmpty(defaultGroup)) {
                throw new RuntimeException("Extract the default group failed! There's nothing between 2 '/'!");
            } else {
                return defaultGroup;
            }
        } catch (Exception e) {
            Logger.warning("", "Failed to extract default group! " + e.getMessage());
            return null;
        }
    }

    /**
     * Use router navigation.
     *
     * @param context     Activity or null.
     * @param mail    Route metas
     * @param requestCode RequestCode
     * @param callback    cb
     */
    protected Object navigation(final Context context, final Mail mail, final int requestCode, final NavigationCallback callback) {
        //try {
            completion(mail);
//        } catch (NoRouteFoundException ex) {
//            logger.warning(Consts.TAG, ex.getMessage());
//
//            if (debuggable()) { // Show friendly tips for user.
//                Toast.makeText(mContext, "There's no route matched!\n" +
//                        " Path = [" + postcard.getPath() + "]\n" +
//                        " Group = [" + postcard.getGroup() + "]", Toast.LENGTH_LONG).show();
//            }
//
//            if (null != callback) {
//                callback.onLost(postcard);
//            } else {    // No callback for this invoke, then we use the global degrade service.
//                DegradeService degradeService = ARouter.getInstance().navigation(DegradeService.class);
//                if (null != degradeService) {
//                    degradeService.onLost(context, postcard);
//                }
//            }
//
//            return null;
//        }

        if (null != callback) {
            callback.onFound(mail);
        }

        if (!mail.isGreenChannel()) {   // It must be run in async thread, maybe interceptor cost too mush time made ANR.
            interceptorService.doInterceptions(mail, new InterceptorCallback() {
                /**
                 * Continue process
                 *
                 * @param mail route meta
                 */
                @Override
                public void onContinue(Mail mail) {
                    doNavigation(context, mail, requestCode, callback);
                }

                /**
                 * Interrupt process, pipeline will be destory when this method called.
                 *
                 * @param exception Reson of interrupt.
                 */
                @Override
                public void onInterrupt(Throwable exception) {
                    if (null != callback) {
                        callback.onInterrupt(mail);
                    }
                }
            });
        } else {
            return doNavigation(context, mail, requestCode, callback);
        }
        return null;
    }

    private Object doNavigation(final Context context, final Mail mail, final int requestCode, final NavigationCallback callback) {
        final Context currentContext = null == context ? mContext : context;

        switch (mail.getType()) {
            case ACTIVITY:
                // Build intent
                final Intent intent = new Intent(currentContext, mail.getDestination());
                intent.putExtras(mail.getExtras());

                // Set flags.
                int flags = mail.getFlags();
                if (-1 != flags) {
                    intent.setFlags(flags);
                } else if (!(currentContext instanceof Activity)) {    // Non activity, need less one flag.
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }

                // Set Actions
                String action = mail.getAction();
                if (!TextUtils.isEmpty(action)) {
                    intent.setAction(action);
                }

                // Navigation in main looper.
                if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(requestCode, currentContext, intent, mail, callback);
                        }
                    });
                } else {
                    startActivity(requestCode, currentContext, intent, mail, callback);
                }

                break;
            case PROVIDER:
                return mail.getProvider();
            case CONTENT_PROVIDER:
            case SERVICE:
            default:
        }
        return null;
    }

    /**
     * Completion the mail by route metas
     *
     * @param mail Incomplete postcard, should complete by this method.
     */
    public synchronized void completion(Mail mail) {
        if (null == mail) {
            throw new RuntimeException("navigation No mail");
        }
        RouteMeta routeMeta = routes.get(mail.getPath());
        if (null == routeMeta) {    // Maybe its does't exist, or didn't load.
            Class<? extends RouteGroup> groupMeta = groupsIndex.get(mail.getGroup());  // Load route meta.
            if (null == groupMeta) {
                throw new RuntimeException("There is no route match the path [" + mail.getPath() + "], in group [" + mail.getGroup() + "]");
            } else {
                // Load route and cache it into memory, then delete from metas.
                try {
                    RouteGroup groupInstance = groupMeta.getConstructor().newInstance();
                    groupInstance.load(routes);
                    groupsIndex.remove(mail.getGroup());

                } catch (Exception e) {
                    throw new RuntimeException("Fatal exception when loading group meta. [" + e.getMessage() + "]");
                }

                completion(mail);   // Reload
            }
        } else {
            mail.setDestination(routeMeta.getDestination());
            mail.setType(routeMeta.getType());
            mail.setPriority(routeMeta.getPriority());
            mail.setExtra(routeMeta.getExtra());

            switch (routeMeta.getType()) {
                case PROVIDER:  // if the route is provider, should find its instance
                    // Its provider, so it must implement IProvider
                    Class<? extends Provider> providerMeta = (Class<? extends Provider>) routeMeta.getDestination();
                    Provider p = providers.get(providerMeta);
                    if (null == p) { // There's no instance of this provider
                        try {
                            Provider provider = providerMeta.getConstructor().newInstance();
                            provider.init(mContext);
                            providers.put(providerMeta, provider);
                            p = provider;
                        } catch (Exception e) {
                            throw new RuntimeException("Init provider failed! " + e.getMessage());
                        }
                    }
                    mail.setProvider(p);
                    break;
                default:
                    break;
            }
        }
    }

    private void startActivity(int requestCode, Context currentContext, Intent intent, Mail mail, NavigationCallback callback) {
        if (requestCode >= 0) {  // Need start for result
            ActivityCompat.startActivityForResult((Activity) currentContext, intent, requestCode, null);
        } else {
            ActivityCompat.startActivity(currentContext, intent, null);
        }

        if (null != callback) { // Navigation over.
            callback.onArrival(mail);
        }
    }

    static void clear() {
        routes.clear();
        groupsIndex.clear();
        providers.clear();
        providersIndex.clear();
        interceptors.clear();
        interceptorsIndex.clear();
    }
}
