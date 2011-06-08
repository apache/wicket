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
package org.apache.wicket.settings.def;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IComponentAwareEventSink;
import org.apache.wicket.IDetachListener;
import org.apache.wicket.IEventDispatcher;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.event.IEventSink;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.apache.wicket.settings.IFrameworkSettings;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class FrameworkSettings implements IFrameworkSettings
{
	private IDetachListener detachListener;

	private List<IEventDispatcher> eventDispatchers = null;

	/**
	 * The {@link ISerializer} that will be used to convert the pages to/from byte arrays
	 */
	private ISerializer pageSerializer;

	/**
	 * Construct.
	 * 
	 * @param application
	 */
	public FrameworkSettings(final Application application)
	{
		pageSerializer = new JavaSerializer(application.getApplicationKey());
	}

	/**
	 * @see org.apache.wicket.settings.IFrameworkSettings#getVersion()
	 */
	public String getVersion()
	{
		String implVersion = null;
		Package pkg = getClass().getPackage();
		if (pkg != null)
		{
			implVersion = pkg.getImplementationVersion();
		}
		return Strings.isEmpty(implVersion) ? "n/a" : implVersion;
	}

	/**
	 * @see org.apache.wicket.settings.IFrameworkSettings#getDetachListener()
	 */
	public IDetachListener getDetachListener()
	{
		return detachListener;
	}

	/**
	 * @see org.apache.wicket.settings.IFrameworkSettings#setDetachListener(org.apache.wicket.IDetachListener)
	 */
	public void setDetachListener(IDetachListener detachListener)
	{
		this.detachListener = detachListener;
	}

	public void add(IEventDispatcher dispatcher)
	{
		Args.notNull(dispatcher, "dispatcher");
		if (eventDispatchers == null)
		{
			eventDispatchers = new ArrayList<IEventDispatcher>();
		}
		if (!eventDispatchers.contains(dispatcher))
		{
			eventDispatchers.add(dispatcher);
		}
	}

	/**
	 * Dispatches event to registered dispatchers
	 * 
	 * @see IEventDispatcher#dispatchEvent(Object, IEvent, Component)
	 * 
	 * @param sink
	 * @param event
	 * @param component
	 */
	public void dispatchEvent(Object sink, IEvent<?> event, Component component)
	{
		// direct delivery
		if (component != null && sink instanceof IComponentAwareEventSink)
		{
			((IComponentAwareEventSink)sink).onEvent(component, event);
		}
		else if (sink instanceof IEventSink)
		{
			((IEventSink)sink).onEvent(event);
		}

		// additional dispatchers delivery
		if (eventDispatchers == null)
		{
			return;
		}
		for (IEventDispatcher dispatcher : eventDispatchers)
		{
			dispatcher.dispatchEvent(sink, event, component);
		}
	}


	public void setSerializer(ISerializer pageSerializer)
	{
		this.pageSerializer = Args.notNull(pageSerializer, "pageSerializer");
	}

	public ISerializer getSerializer()
	{
		return pageSerializer;
	}
}
