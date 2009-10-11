package org.apache.wicket.ng;

import org.apache.wicket.ng.request.cycle.RequestCycle;

public class ThreadContext
{
	private ThreadContext()
	{

	}

	private Application application;

	private RequestCycle requestCycle;

	private Session session;

	private static ThreadLocal<ThreadContext> threadLocal = new ThreadLocal<ThreadContext>();

	private static ThreadContext get(boolean createIfDoesNotExist)
	{
		ThreadContext context = threadLocal.get();
		if (createIfDoesNotExist && context == null)
		{
			context = new ThreadContext();
			threadLocal.set(context);
		}
		return context;
	}

	public static Application getApplication()
	{
		ThreadContext context = get(false);
		return context != null ? context.application : null;
	}

	public static void setApplication(Application application)
	{
		ThreadContext context = get(true);
		context.application = application;
	}

	public static RequestCycle getRequestCycle()
	{
		ThreadContext context = get(false);
		return context != null ? context.requestCycle : null;
	}

	public static void setRequestCycle(RequestCycle requestCycle)
	{
		ThreadContext context = get(true);
		context.requestCycle = requestCycle;
	}

	public static Session getSession()
	{
		ThreadContext context = get(false);
		return context != null ? context.session : null;
	}
	
	public static void setSession(Session session)
	{
		ThreadContext context = get(true);
		context.session = session;
	}

	public static void detach()
	{
		threadLocal.remove();
	}
	
	public static ThreadContext getAndClean()
	{
		ThreadContext value = threadLocal.get();
		threadLocal.remove();
		return value;
	}
	
	public static void restore(ThreadContext threadContext)
	{
		threadLocal.set(threadContext);	
	}
}
