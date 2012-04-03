package org.apache.wicket.atmosphere;

import java.lang.reflect.Method;
import java.util.Objects;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class EventSubscription
{
	private String componentPath;

	private String methodName;

	private Predicate<Object> filter;

	public EventSubscription(Component component, Method method)
	{
		componentPath = component.getPageRelativePath();
		Class< ? > eventType = method.getParameterTypes()[1];
		filter = Predicates.and(Predicates.instanceOf(eventType), createFilter(method));
		methodName = method.getName();
	}

	@SuppressWarnings("unchecked")
	private static Predicate<Object> createFilter(Method method)
	{
		Subscribe subscribe = method.getAnnotation(Subscribe.class);
		try
		{
			return (Predicate<Object>) subscribe.filter().newInstance();
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

	public String getComponentPath()
	{
		return componentPath;
	}

	public Predicate<Object> getFilter()
	{
		return filter;
	}

	public String getMethodName()
	{
		return methodName;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(componentPath, methodName);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof EventSubscription)
		{
			EventSubscription other = (EventSubscription) obj;
			return Objects.equals(componentPath, other.getComponentPath())
				&& Objects.equals(methodName, other.getMethodName());
		}
		return false;
	}
}
