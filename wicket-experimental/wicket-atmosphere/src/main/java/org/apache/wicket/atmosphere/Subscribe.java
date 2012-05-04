package org.apache.wicket.atmosphere;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.cycle.RequestCycle;

import com.google.common.base.Predicate;

/**
 * Subscribes a method on a component to receive events of a certain type. The method should have 2
 * parameters: the first must be {@link AjaxRequestTarget}, the second defines the type of events to
 * receive. This method will receive any event posted to the {@link EventBus} if it matches the type
 * of the second parameter and the filter accepts it. Any context a Wicket component expects to be
 * available, such as the {@link RequestCycle} and {@link Session}, is accessible on invocation of
 * the method.
 * 
 * <p>
 * Annotated methods will automatically be detected by {@link AtmosphereEventSubscriptionCollector}.
 * The page on which the component is placed will get a {@link AtmosphereBehavior}, which sets up a
 * persistent connection (for example websocket or streaming http).
 * 
 * @author papegaaij
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {
	/**
	 * An optional filter on events to be received by the method. The filter cannot rely on any
	 * context. For example, the {@link RequestCycle} may not be available.
	 * 
	 * @return The filter on events, defaults to no filter.
	 */
	Class<? extends Predicate<?>> filter() default NoFilterPredicate.class;
}
