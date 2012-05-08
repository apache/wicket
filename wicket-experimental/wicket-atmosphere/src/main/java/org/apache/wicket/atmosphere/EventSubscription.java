package org.apache.wicket.atmosphere;

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * The subscription of a method on a component to certain events. This is used by {@link EventBus}
 * to track the subscriptions.
 * 
 * @author papegaaij
 */
public class EventSubscription
{
	private String componentPath;

	private String methodName;

	private Predicate<Object> filter;

	/**
	 * Construct.
	 * 
	 * @param component
	 * @param method
	 */
	public EventSubscription(Component component, Method method)
	{
		componentPath = component.getPageRelativePath();
		Class<?> eventType = method.getParameterTypes()[1];
		filter = Predicates.and(Predicates.instanceOf(eventType), createFilter(method));
		methodName = method.getName();
	}

	@SuppressWarnings("unchecked")
	private static Predicate<Object> createFilter(Method method)
	{
		Subscribe subscribe = method.getAnnotation(Subscribe.class);
		try
		{
			return (Predicate<Object>)subscribe.filter().newInstance();
		}
		catch (InstantiationException e)
		{
			throw new WicketRuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * @return The path of the subscribed component
	 */
	public String getComponentPath()
	{
		return componentPath;
	}

	/**
	 * @return The filter on incomming events, a combination of the type and the
	 *         {@link Subscribe#filter()} parameter.
	 */
	public Predicate<Object> getFilter()
	{
		return filter;
	}

	/**
	 * @return The method that is subscribed
	 */
	public String getMethodName()
	{
		return methodName;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(componentPath, methodName);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof EventSubscription)
		{
			EventSubscription other = (EventSubscription)obj;
			return Objects.equal(componentPath, other.getComponentPath()) &&
				Objects.equal(methodName, other.getMethodName());
		}
		return false;
	}
}
