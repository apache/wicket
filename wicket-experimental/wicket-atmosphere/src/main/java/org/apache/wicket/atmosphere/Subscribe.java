package org.apache.wicket.atmosphere;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.common.base.Predicate;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe
{
	Class< ? extends Predicate< ? >> filter() default NoFilterPredicate.class;
}
