package com.friedust.hawaproperties;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * TypeCaster tries to cast String value to a given type.
 * 
 * @author frieddust
 *
 */
public class TypeCaster {

    private TypeCaster() {
    }

    /**
     * 
     * @param value
     *            a string value to be casted
     * @param type
     *            type to be converted to
     * @return an instance of given type
     * 
     * @exception ClassCastException
     *                if given type neither has a public static method valueOf nor has a constructor with single argument of String
     */
    public static <T> Object cast(String value, Class<T> type) {
        if (value == null)
            return null;
        if (type.equals(String.class))
            return value;

        Method method = null;
        try {
            method = type.getMethod("valueOf", String.class);
            return invoke(method, value);
        } catch (NoSuchMethodException e) {
        }

        Constructor<T> constructor = null;
        try {
            constructor = type.getConstructor(String.class);
            return invoke(constructor, value);
        } catch (NoSuchMethodException e) {
        } catch (SecurityException e) {
        }

        throw new ClassCastException(
                type.getName() + " neither has a public static method valueOf nor has a constructor with single argument of String");
    }

    private static Object invoke(Method method, String value) {
        try {
            return method.invoke(null, value);
        } catch (Exception e) {
            throw new ClassCastException("Cannot convert to " + method.getReturnType().getName());
        }
    }

    private static <T> Object invoke(Constructor<T> constructor, String value) {
        try {
            return constructor.newInstance(value);
        } catch (Exception e) {
            throw new ClassCastException("Cannot convert to " + constructor.getDeclaringClass().getName());
        }
    }
}
