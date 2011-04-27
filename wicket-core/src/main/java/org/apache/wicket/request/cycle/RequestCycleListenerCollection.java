/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.request.cycle;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.util.listener.ListenerCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Special, Wicket internal composite {@link IRequestCycleListener} that
 */
public class RequestCycleListenerCollection extends ListenerCollection<IRequestCycleListener>
	implements
		IRequestCycleListener
{
	private static final Logger logger = LoggerFactory.getLogger(RequestCycleListenerCollection.class);
	private static final long serialVersionUID = 1L;

	public void onBeginRequest(final RequestCycle cycle)
	{
		notify(new INotifier<IRequestCycleListener>()
		{
			public void notify(IRequestCycleListener listener)
			{
				listener.onBeginRequest(cycle);
			}
		});
	}

	public void onEndRequest(final RequestCycle cycle)
	{
		reversedNotify(new INotifier<IRequestCycleListener>()
		{
			public void notify(IRequestCycleListener listener)
			{
				listener.onEndRequest(cycle);
			}
		});
	}

	/**
	 * Notifies all registered listeners of the exception and calls the first handler that was
	 * returned by the listeners.
	 * 
	 * @see org.apache.wicket.request.cycle.IRequestCycleListener#onException(org.apache.wicket.request.cycle.RequestCycle,
	 *      java.lang.Exception)
	 */
	public IRequestHandler onException(final RequestCycle cycle, final Exception ex)
	{
		final List<IRequestHandler> handlers = new ArrayList<IRequestHandler>();

		notify(new INotifier<IRequestCycleListener>()
		{
			public void notify(IRequestCycleListener listener)
			{
				IRequestHandler handler = listener.onException(cycle, ex);
				if (handler != null)
				{
					handlers.add(handler);
				}
			}
		});

		if (handlers.isEmpty())
		{
			return null;
		}
		if (handlers.size() > 1)
		{
			logger.debug(
				"{} exception handlers available for exception {}, using the first handler",
				handlers.size(), ex);
		}
		return handlers.get(0);
	}

	public void onDetach(final RequestCycle cycle)
	{
		reversedNotify(new INotifier<IRequestCycleListener>()
		{
			public void notify(IRequestCycleListener listener)
			{
				try
				{
					listener.onDetach(cycle);
				}
				catch (Exception e)
				{
					logger.error("Error detaching request cycle listener: " + listener, e);
				}
			}
		});
	}

	public void onRequestHandlerResolved(final IRequestHandler handler)
	{
		notify(new INotifier<IRequestCycleListener>()
		{
			public void notify(IRequestCycleListener listener)
			{
				listener.onRequestHandlerResolved(handler);
			}
		});
	}

	public void onExceptionRequestHandlerResolved(final IRequestHandler handler,
		final Exception exception)
	{
		notify(new INotifier<IRequestCycleListener>()
		{
			public void notify(IRequestCycleListener listener)
			{
				listener.onExceptionRequestHandlerResolved(handler, exception);
			}
		});
	}

	public void onRequestHandlerScheduled(final IRequestHandler handler)
	{
		notify(new INotifier<IRequestCycleListener>()
		{
			public void notify(IRequestCycleListener listener)
			{
				listener.onRequestHandlerScheduled(handler);
			}
		});
	}

}
