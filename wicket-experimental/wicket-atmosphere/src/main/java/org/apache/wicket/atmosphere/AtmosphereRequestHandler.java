package org.apache.wicket.atmosphere;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;

/**
 * Handles pseudo requests triggered by an event. An {@link AjaxRequestTarget} is scheduled and the
 * subscribed methods are invoked.
 * 
 * @author papegaaij
 */
public class AtmosphereRequestHandler implements IRequestHandler
{
	private PageKey pageKey;

	private Object event;

	private Collection<EventSubscription> subscriptions;

	/**
	 * Construct.
	 * 
	 * @param pageKey
	 * @param subscriptions
	 * @param event
	 */
	public AtmosphereRequestHandler(PageKey pageKey, Collection<EventSubscription> subscriptions,
		Object event)
	{
		this.pageKey = pageKey;
		this.subscriptions = subscriptions;
		this.event = event;
	}

	@Override
	public void respond(IRequestCycle requestCycle)
	{
		Page page = (Page)Application.get().getMapperContext().getPageInstance(pageKey.getPageId());
		AjaxRequestTarget target = WebApplication.get().newAjaxRequestTarget(page);
		requestCycle.scheduleRequestHandlerAfterCurrent(target);
		executeHandlers(target, page);
	}

	private void executeHandlers(AjaxRequestTarget target, Page page)
	{
		for (EventSubscription curSubscription : subscriptions)
		{
			Component component = page.get(curSubscription.getComponentPath());
			for (Method curMethod : component.getClass().getMethods())
			{
				if (curMethod.isAnnotationPresent(Subscribe.class) &&
					curMethod.getName().equals(curSubscription.getMethodName()))
				{
					try
					{
						curMethod.invoke(component, target, event);
					}
					catch (IllegalAccessException e)
					{
						throw new WicketRuntimeException(e);
					}
					catch (IllegalArgumentException e)
					{
						throw new WicketRuntimeException(e);
					}
					catch (InvocationTargetException e)
					{
						throw new WicketRuntimeException(e);
					}
				}
			}
		}
	}

	@Override
	public void detach(IRequestCycle requestCycle)
	{
	}
}
