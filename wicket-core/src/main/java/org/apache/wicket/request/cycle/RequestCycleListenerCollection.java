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
import org.apache.wicket.request.Url;
import org.apache.wicket.util.listener.ListenerCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Composite {@link IRequestCycleListener} that notifies all registered listeners with each
 * IRequestCycleListener event.
 * <p>
 * <h3>Order of notification</h3>
 * <p>
 * {@link #onBeginRequest(RequestCycle)}, {@link #onRequestHandlerScheduled(RequestCycle, IRequestHandler)} and
 * {@link #onRequestHandlerResolved(RequestCycle, IRequestHandler)} are notified in first in, first out order.
 * <p>
 * {@link #onEndRequest(RequestCycle)} and {@link #onDetach(RequestCycle)} are notified in last in
 * first out order (i.e. reversed order). So for these events the collection functions as a stack.
 * <p>
 * <h3>Exception handling</h3>
 * <p>
 * The {@code RequestCycleListenerCollection} will use the first exception handler that is returned
 * from all listeners in {@link #onException(RequestCycle, Exception)}
 */
public class RequestCycleListenerCollection extends ListenerCollection<IRequestCycleListener>
	implements
		IRequestCycleListener
{
	private static final Logger logger = LoggerFactory.getLogger(RequestCycleListenerCollection.class);
	private static final long serialVersionUID = 1L;

	/**
	 * Notifies all registered listeners of the onBeginRequest event in first in first out order,
	 * i.e. the listener that is the first element of this collection is the first listener to be
	 * notified of {@code onBeginRequest}.
	 */
	@Override
	public void onBeginRequest(final RequestCycle cycle)
	{
		notify(new INotifier<IRequestCycleListener>()
		{
			@Override
			public void notify(IRequestCycleListener listener)
			{
				listener.onBeginRequest(cycle);
			}
		});
	}

	/**
	 * Notifies all registered listeners of the {@code onEndRequest} event in first in last out
	 * order (i.e. the last listener that received an {@code #onBeginRequest} will be the first to
	 * get notified of an {@code onEndRequest}.
	 * 
	 * @see IRequestCycleListener#onEndRequest(RequestCycle)
	 */
	@Override
	public void onEndRequest(final RequestCycle cycle)
	{
		reversedNotify(new INotifier<IRequestCycleListener>()
		{
			@Override
			public void notify(IRequestCycleListener listener)
			{
				listener.onEndRequest(cycle);
			}
		});
	}

	/**
	 * Notifies all registered listeners of the {@code onDetach} event in first in last out order
	 * (i.e. the last listener that received an {@code #onBeginRequest} will be the first to get
	 * notified of an {@code onDetach}.
	 * 
	 * @see IRequestCycleListener#onDetach(RequestCycle)
	 */
	@Override
	public void onDetach(final RequestCycle cycle)
	{
		reversedNotifyIgnoringExceptions(new INotifier<IRequestCycleListener>()
		{
			@Override
			public void notify(IRequestCycleListener listener)
			{
				listener.onDetach(cycle);
			}
		});
	}

	/**
	 * Notifies all registered listeners of the exception and calls the first handler that was
	 * returned by the listeners.
	 * 
	 * @see IRequestCycleListener#onException(RequestCycle, Exception)
	 */
	@Override
	public IRequestHandler onException(final RequestCycle cycle, final Exception ex)
	{
		final List<IRequestHandler> handlers = new ArrayList<IRequestHandler>();

		notify(new INotifier<IRequestCycleListener>()
		{
			@Override
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

	@Override
	public void onRequestHandlerResolved(final RequestCycle cycle, final IRequestHandler handler)
	{
		notify(new INotifier<IRequestCycleListener>()
		{
			@Override
			public void notify(IRequestCycleListener listener)
			{
				listener.onRequestHandlerResolved(cycle, handler);
			}
		});
	}

	@Override
	public void onExceptionRequestHandlerResolved(final RequestCycle cycle,
		final IRequestHandler handler, final Exception exception)
	{
		notify(new INotifier<IRequestCycleListener>()
		{
			@Override
			public void notify(IRequestCycleListener listener)
			{
				listener.onExceptionRequestHandlerResolved(cycle, handler, exception);
			}
		});
	}

	@Override
	public void onRequestHandlerScheduled(final RequestCycle cycle, final IRequestHandler handler)
	{
		notify(new INotifier<IRequestCycleListener>()
		{
			@Override
			public void notify(IRequestCycleListener listener)
			{
				listener.onRequestHandlerScheduled(cycle, handler);
			}
		});
	}

	@Override
	public void onRequestHandlerExecuted(final RequestCycle cycle, final IRequestHandler handler)
	{
		notify(new INotifier<IRequestCycleListener>()
		{
			@Override
			public void notify(IRequestCycleListener listener)
			{
				listener.onRequestHandlerExecuted(cycle, handler);
			}
		});
	}

	@Override
	public void onUrlMapped(final RequestCycle cycle, final IRequestHandler handler, final Url url)
	{
		notify(new INotifier<IRequestCycleListener>()
		{
			@Override
			public void notify(IRequestCycleListener listener)
			{
				listener.onUrlMapped(cycle, handler, url);
			}
		});

	}
}
