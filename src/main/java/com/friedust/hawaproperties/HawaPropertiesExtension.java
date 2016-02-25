package com.friedust.hawaproperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

import com.friedust.hawaproperties.annotation.PropertySource;
import com.friedust.hawaproperties.annotation.PropertyValue;

/**
 * CDI extension to read properties file from classpath/filesystem
 * mentioned in {@link PropertySource} and inject the values in attributes
 * annotated with {@link PropertyValue}
 * 
 * @author frieddust
 *
 */
public class HawaPropertiesExtension implements Extension {

    public <T> void initializePropertyLoading(@Observes ProcessInjectionTarget<T> pit) {
        AnnotatedType<T> at = pit.getAnnotatedType();
        if (!at.isAnnotationPresent(PropertySource.class)) {
            return;
        }

        PropertySource source = at.getAnnotation(PropertySource.class);
        String filename = source.value();

        Properties properties = loadProperties(filename);

        Map<Field, String> fieldAndValues = loadValues(at, properties);

        InjectionTarget<T> newInjectionTarget = inject(pit, fieldAndValues);
        pit.setInjectionTarget(newInjectionTarget);
    }

    private <T> InjectionTarget<T> inject(final ProcessInjectionTarget<T> pit, final Map<Field, String> fieldAndValues) {
        final InjectionTarget<T> injectionTarget = pit.getInjectionTarget();
        return new InjectionTarget<T>() {

            public void inject(T instance, CreationalContext<T> ctx) {
                injectionTarget.inject(instance, ctx);

                try {
                    for (Map.Entry<Field, String> attr : fieldAndValues.entrySet()) {
                        Field field = attr.getKey();
                        field.setAccessible(true);

                        String valueAsString = attr.getValue();

                        Class<?> type = field.getType();

                        Object value = TypeCaster.cast(valueAsString, type);

                        field.set(instance, value);
                    }
                } catch (ClassCastException cce) {
                    pit.addDefinitionError(new InjectionException(cce));
                } catch (IllegalArgumentException e) {
                    pit.addDefinitionError(new InjectionException(e));
                } catch (IllegalAccessException e) {
                    pit.addDefinitionError(new InjectionException(e));
                }
            }

            public T produce(CreationalContext<T> ctx) {
                return injectionTarget.produce(ctx);
            }

            public void dispose(T instance) {
                injectionTarget.dispose(instance);
            }

            public Set<InjectionPoint> getInjectionPoints() {
                return injectionTarget.getInjectionPoints();
            }

            public void postConstruct(T instance) {
                injectionTarget.postConstruct(instance);
            }

            public void preDestroy(T instance) {
                injectionTarget.dispose(instance);
            }
        };
    }

    /**
     * 
     * @param filename
     *            It should either start with classpath:filename.ext or /path/to/file.ext
     * @return {@link Properties} properties loaded with key/values from the given file
     */
    private Properties loadProperties(String filename) {
        try {
            InputStream stream;
            if (filename.startsWith("classpath:")) {
                String file = filename.substring("classpath:".length());
                stream = getClass().getResourceAsStream("/" + file);
            } else {

                stream = new FileInputStream(filename);
            }

            Properties properties = new Properties();
            properties.load(stream);

            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private <T> Map<Field, String> loadValues(AnnotatedType<T> annotatedType, Properties properties) {
        Set<AnnotatedField<? super T>> fields = annotatedType.getFields();
        Map<Field, String> fieldAndValues = new HashMap<Field, String>();

        for (AnnotatedField<?> annotatedField : fields) {
            if (annotatedField.isAnnotationPresent(PropertyValue.class)) {
                String attrKey = annotatedField.getAnnotation(PropertyValue.class).value();

                Field field = annotatedField.getJavaMember();
                String value = (String) properties.get(attrKey);
                fieldAndValues.put(field, value);
            }
        }

        return fieldAndValues;
    }
}
