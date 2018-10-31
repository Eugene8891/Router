package com.midrive.voice.router_api.core;

import android.content.Context;
import com.midrive.voice.router_annotation.annotation.Route;
import com.midrive.voice.router_api.service.InterceptorService;
import com.midrive.voice.router_api.template.Interceptor;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * All of interceptors
 *
 */
@Route(path = "/router/service/interceptor")
public class InterceptorServiceImpl implements InterceptorService {
    private static boolean interceptorHasInit;
    private static final Object interceptorInitLock = new Object();

    @Override
    public void doInterceptions(final Mail mail, final InterceptorCallback callback) {
        if (RouteBus.getInstance().getInterceptors().size() > 0) {

            checkInterceptorsInitStatus();

            if (!interceptorHasInit) {
                callback.onInterrupt(new RuntimeException("Interceptors initialization takes too much time."));
                return;
            }

            RouteBus.getInstance().getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    CountDownLatch interceptorCounter = new CountDownLatch(RouteBus.getInstance().getInterceptors().size());
                    try {
                        excute(0, interceptorCounter, mail);
                        interceptorCounter.await(mail.getTimeout(), TimeUnit.SECONDS);
                        if (interceptorCounter.getCount() > 0) {    // Cancel the navigation this time, if it hasn't return anythings.
                            callback.onInterrupt(new RuntimeException("The interceptor processing timed out."));
                        } else if (null != mail.getTag()) {    // Maybe some exception in the tag.
                            callback.onInterrupt(new RuntimeException(mail.getTag().toString()));
                        } else {
                            callback.onContinue(mail);
                        }
                    } catch (Exception e) {
                        callback.onInterrupt(e);
                    }
                }
            });
        } else {
            callback.onContinue(mail);
        }
    }

    /**
     * Excute interceptor
     *
     * @param index    current interceptor index
     * @param counter  interceptor counter
     * @param mail routeMeta
     */
    private static void excute(final int index, final CountDownLatch counter, final Mail mail) {
        if (index < RouteBus.getInstance().getInterceptors().size()) {
            Interceptor interceptor = RouteBus.getInstance().getInterceptors().get(index);
            interceptor.process(mail, new InterceptorCallback() {
                @Override
                public void onContinue(Mail mail) {
                    // Last interceptor excute over with no exception.
                    counter.countDown();
                    excute(index + 1, counter, mail);  // When counter is down, it will be execute continue ,but index bigger than interceptors size, then U know.
                }

                @Override
                public void onInterrupt(Throwable exception) {
                    // Last interceptor excute over with fatal exception.

                    mail.setTag(null == exception ? new RuntimeException("No message.") : exception.getMessage());    // save the exception message for backup.
                    while (counter.getCount() > 0) {
                        counter.countDown();
                    }
                    // Be attention, maybe the thread in callback has been changed,
                    // then the catch block(L207) will be invalid.
                    // The worst is the thread changed to main thread, then the app will be crash, if you throw this exception!
//                    if (!Looper.getMainLooper().equals(Looper.myLooper())) {    // You shouldn't throw the exception if the thread is main thread.
//                        throw new HandlerException(exception.getMessage());
//                    }
                }
            });
        }
    }

    @Override
    public void init(final Context context) {
        RouteBus.getInstance().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (RouteBus.getInstance().getInterceptorsIndex().size() > 0) {
                    for (Map.Entry<Integer, Class<? extends Interceptor>> entry : RouteBus.getInstance().getInterceptorsIndex().entrySet()) {
                        Class<? extends Interceptor> interceptorClass = entry.getValue();
                        try {
                            Interceptor interceptor = interceptorClass.getConstructor().newInstance();
                            interceptor.init(context);
                            RouteBus.getInstance().getInterceptors().add(interceptor);
                        } catch (Exception ex) {
                            throw new RuntimeException("Router init interceptor error! name = [" + interceptorClass.getName() + "], reason = [" + ex.getMessage() + "]");
                        }
                    }
                    interceptorHasInit = true;

                    synchronized (interceptorInitLock) {
                        interceptorInitLock.notifyAll();
                    }
                }
            }
        });
    }

    private static void checkInterceptorsInitStatus() {
        synchronized (interceptorInitLock) {
            while (!interceptorHasInit) {
                try {
                    interceptorInitLock.wait(1 * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interceptor init cost too much time error! reason = [" + e.getMessage() + "]");
                }
            }
        }
    }
}
