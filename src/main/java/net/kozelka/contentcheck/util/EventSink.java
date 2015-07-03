package net.kozelka.contentcheck.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Simple event sink.</p>
 * <p>Proxies given listener interface, by distributing all received events to all registered listeners.</p>
 *
 * @author Petr Kozelka
 */
public class EventSink<T> {
    private final List<T> listeners = new ArrayList<T>();

    /**
     * The proxy object; each of its method calls all registered listeners.
     * Listeners are called in the order of registration; any thrown exception stops further sending.
     */
    public final T fire;

    /**
     * Creates simple event sink for given listener class.
     * @param listenerClass -
     * @return event sink
     */
    public static <T> EventSink<T> create(Class<T> listenerClass) {
        return create(listenerClass, EventSink.class.getClassLoader());
    }

    public static <T> EventSink<T> create(Class<T> listenerClass, ClassLoader classLoader) {
        return new EventSink<T>(listenerClass, classLoader);
    }

    @SuppressWarnings("unchecked")
    private EventSink(Class<T> listenerClass, ClassLoader classLoader) {
        fire = (T) Proxy.newProxyInstance(classLoader, new Class[]{listenerClass}, handler);
    }

    /**
     * Registers a listener. Does not check for duplicates.
     * @param listener -
     */
    public void addListener(T listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener from the registry.
     * @param listener -
     */
    public void removeListener(T listener) {
        listeners.remove(listener);
    }

    private final InvocationHandler handler = new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            for (T listener : listeners) {
                method.invoke(listener, args);
                //todo swallow unchecked exceptions?
            }
            return null;
        }
    };
}
