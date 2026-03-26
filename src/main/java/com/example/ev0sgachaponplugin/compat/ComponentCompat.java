package com.example.ev0sgachaponplugin.compat;

import java.lang.reflect.Method;

public final class ComponentCompat {

    private ComponentCompat() {
    }

    public static Object getBlockComponent(Object chunk, int x, int y, int z, Class<?> componentClass) {
        if (chunk == null) {
            return null;
        }

        Class<?> chunkClass = chunk.getClass();
        String[] methodNames = new String[]{
                "getComponentAt",
                "getBlockComponent",
                "getBlockComponentAt",
                "getComponent",
                "getBlockData",
                "getBlockStateAt"
        };

        for (String methodName : methodNames) {
            try {
                Method method = chunkClass.getMethod(methodName, int.class, int.class, int.class, Class.class);
                Object value = method.invoke(chunk, x, y, z, componentClass);
                if (value != null && componentClass.isInstance(value)) {
                    return value;
                }
            } catch (Throwable ignored) {
            }
        }

        for (String methodName : methodNames) {
            try {
                Method method = chunkClass.getMethod(methodName, int.class, int.class, int.class);
                Object value = method.invoke(chunk, x, y, z);
                if (value != null && componentClass.isInstance(value)) {
                    return value;
                }
            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    public static void registerComponent(Class<?> componentClass, String id, Object codec) {
        try {
            Class<?> registryClass = Class.forName("com.hypixel.hytale.component.ComponentRegistry");
            Method getInstance = null;
            try {
                getInstance = registryClass.getMethod("getInstance");
            } catch (Throwable ignored) {
            }

            Object instance = null;
            if (getInstance != null) {
                instance = getInstance.invoke(null);
            }
            if (instance == null) {
                try {
                    instance = registryClass.getField("INSTANCE").get(null);
                } catch (Throwable ignored) {
                }
            }

            if (instance == null) {
                return;
            }

            for (Method method : registryClass.getMethods()) {
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                if (!method.getName().toLowerCase(java.util.Locale.ROOT).contains("register")) {
                    continue;
                }
                try {
                    method.invoke(instance, componentClass, id, codec);
                    return;
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
    }
}