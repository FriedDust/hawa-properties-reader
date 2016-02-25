package com.friedust.hawaproperties.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Value should either start with classpath:filename.ext or /path/to/file.ext
 * 
 * @author frieddust
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertySource {

    String value();
}
