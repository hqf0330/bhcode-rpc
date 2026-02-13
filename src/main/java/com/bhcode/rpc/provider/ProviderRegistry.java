package com.bhcode.rpc.provider;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hqf0330@gmail.com
 */
public class ProviderRegistry {

    private final Map<String, Invocation<?>> serviceInstanceMap = new ConcurrentHashMap<>();

    public <I> void register(Class<I> interfaceClass, I serviceInstance) {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException(interfaceClass.getName() + " is not an interface");
        }

        if (serviceInstanceMap.putIfAbsent(interfaceClass.getName(), new Invocation<>(interfaceClass,
                serviceInstance)) != null) {
            throw new IllegalArgumentException(interfaceClass.getName() + " is already registered");
        }
    }

    public Invocation<?> findService(String serviceName) {
        return serviceInstanceMap.get(serviceName);
    }

    public static class Invocation<I> {

        final I serviceInstance;

        final Class<I> interfaceClass;


        public Invocation(Class<I> interfaceClass, I serviceInstance) {
            this.interfaceClass = interfaceClass;
            this.serviceInstance = serviceInstance;
        }

        public Object invoke(String methodName, Class<?>[] parameterTypes, Object[] params) throws Exception {
            Method invokeMethod = interfaceClass.getDeclaredMethod(methodName, parameterTypes);
            return invokeMethod.invoke(serviceInstance, params);
        }
    }

}
