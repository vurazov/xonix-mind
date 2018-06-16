package com.lineate.xonix.mind.utils;

import lombok.extern.slf4j.Slf4j;

import javax.lang.model.type.NullType;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * ReflectionUtils.invokeConstructor(className);
 * Class represents set of reflection utilities
 */
@Slf4j
public final class ReflectionUtils {
     /**
     * TODO: This approach is not use for  inner class
     * as in constructor.getParameterTypes() returns
     * besides type of parameter, type of outer class in first array's element
     */
    public static <T> T newInstance(String className, ClassLoader classLoader, final Object... params)
            throws ClassNotFoundException, NoSuchMethodException {
        if (className == null) {
            throw new IllegalArgumentException("ClassName must be specified");
        }
        if (params == null) {
            throw new IllegalArgumentException("Arguments must be specified. Use empty array if no arguments");
        }

        Class[] typeParameters = getClasses(params);
        final List<java.lang.reflect.Constructor> listConstructor = new LinkedList<java.lang.reflect.Constructor>();
        final Class<?> clazz = Class.forName(className, false, classLoader);
        for (java.lang.reflect.Constructor constructor : clazz.getDeclaredConstructors()) {
            int numberTypeParams = 0;
            Class[] constructorParameters = constructor.getParameterTypes();
            if (constructorParameters.length == typeParameters.length) {
                for (int i = 0; i < constructorParameters.length; i++) {
                    if (constructorParameters[i].isAssignableFrom(typeParameters[i])) {
                        numberTypeParams++;
                    } else {
                        break;
                    }
                }
                if (numberTypeParams == typeParameters.length) {
                    listConstructor.add(constructor);
                }
            }
        }

        if (listConstructor.size() != 1) {
            throw new NoSuchMethodException("Have no appropriate construtor for class " +
                    className + " with params " + Arrays.asList(typeParameters));
        } else {
            try {
                return AccessController.doPrivileged(new PrivilegedAction<T>() {
                    @Override
                    public T run() {
                        try {
                            listConstructor.get(0).setAccessible(true);
                            return (T) listConstructor.get(0).newInstance(params);
                        } catch (Exception ex) {
                            throw new RuntimeException("Instance of " + clazz.getName() + " doesn't created", ex);
                        }
                    }
                });
            } catch (Throwable t) {
                throw (RuntimeException) unwrap(t);
            }
        }
    }

    private static Class[] getClasses(Object[] params) {
        Class[] result = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            result[i] = getClass(params[i]);
        }
        return result;
    }

    private static Class getClass(Object param) {
        return (param == null) ? NullType.class : param.getClass();
    }

    private static Exception unwrap(Throwable t) {
        if (t instanceof InvocationTargetException) {
            t = ((InvocationTargetException) t).getTargetException();
        }
        if (t instanceof Error) {
            throw (Error) t;
        } else if (t instanceof Exception) {
            return (Exception) t;
        } else {
            return new RuntimeException(t);
        }
    }
}