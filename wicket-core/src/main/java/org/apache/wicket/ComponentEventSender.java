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
package org.apache.wicket;

import java.util.List;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEventSink;
import org.apache.wicket.event.IEventSource;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.util.visit.Visits;

/**
 * Implements event {@link Broadcast}ing
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
final class ComponentEventSender implements IEventSource
{
	private final Component source;
	private final IEventDispatcher dispatcher;

	/**
	 * Constructor
	 * 
	 * @param source
	 *            component that originated the event
	 * @param dispatcher
	 */
	public ComponentEventSender(Component source, IEventDispatcher dispatcher)
	{
		Args.notNull(source, "source");
		Args.notNull(dispatcher, "dispatcher");
		this.source = source;
		this.dispatcher = dispatcher;
	}

	/** {@inheritDoc} */
	@Override
	public <T> void send(IEventSink sink, Broadcast type, T payload)
	{
		ComponentEvent<?> event = new ComponentEvent<T>(sink, source, type, payload);
		Args.notNull(type, "type");
		switch (type)
		{
			case BUBBLE :
				bubble(event);
				break;
			case BREADTH :
				breadth(event);
				break;
			case DEPTH :
				depth(event);
				break;
			case EXACT :
				exact(event);
				break;
		}
	}

	/**
	 * Exact broadcast
	 *
	 * Broadcasts the event only to the sink. In case the sink is a Component
	 * then all its behaviors are notified as well.
	 *
	 * @param event
	 *          The event with the payload
	 */
	private void exact(ComponentEvent<?> event)
	{
		IEventSink sink = event.getSink();

		if (sink instanceof Component)
		{
			dispatchToComponent(dispatcher, (Component) sink, event);
		}
		else
		{
			dispatcher.dispatchEvent(sink, event, null);
		}
	}

	/**
	 * Breadth broadcast
	 * 
	 * @param event
	 */
	private void breadth(final ComponentEvent<?> event)
	{
		IEventSink sink = event.getSink();

		boolean targetsApplication = sink instanceof Application;
		boolean targetsSession = targetsApplication || sink instanceof Session;
		boolean targetsCycle = targetsSession || sink instanceof RequestCycle;
		boolean targetsComponent = sink instanceof Component;

		if (!targetsComponent && !targetsCycle)
		{
			dispatcher.dispatchEvent(sink, event, null);
			return;
		}

		if (targetsApplication)
		{
			dispatcher.dispatchEvent(source.getApplication(), event, null);
		}
		if (event.isStop())
		{
			return;
		}
		if (targetsSession)
		{
			dispatcher.dispatchEvent(source.getSession(), event, null);
		}
		if (event.isStop())
		{
			return;
		}
		if (targetsCycle)
		{
			dispatcher.dispatchEvent(source.getRequestCycle(), event, null);
		}
		if (event.isStop())
		{
			return;
		}

		Component cursor = targetsCycle ? source.getPage() : (Component)sink;

		dispatchToComponent(dispatcher, cursor, event);

		if (event.isStop())
		{
			return;
		}

		event.resetShallow(); // reset shallow flag

		if (cursor instanceof MarkupContainer)
		{
			((MarkupContainer)cursor).visitChildren(new ComponentEventVisitor(event, dispatcher));
		}
	}

	/**
	 * Depth broadcast
	 * 
	 * @param event
	 *            event
	 */
	private void depth(final ComponentEvent<?> event)
	{
		IEventSink sink = event.getSink();

		boolean targetsApplication = sink instanceof Application;
		boolean targetsSession = targetsApplication || sink instanceof Session;
		boolean targetsCycle = targetsSession || sink instanceof RequestCycle;
		boolean targetsComponent = sink instanceof Component;

		if (!targetsComponent && !targetsCycle)
		{
			dispatcher.dispatchEvent(sink, event, null);
			return;
		}

		Component cursor = (targetsCycle) ? source.getPage() : (Component)sink;

		if (cursor instanceof MarkupContainer)
		{
			Visits.visitPostOrder(cursor, new ComponentEventVisitor(event, dispatcher));
		}
		else
		{
			dispatchToComponent(dispatcher, cursor, event);
		}
		if (event.isStop())
		{
			return;
		}
		if (targetsCycle)
		{
			dispatcher.dispatchEvent(source.getRequestCycle(), event, null);
		}
		if (event.isStop())
		{
			return;
		}
		if (targetsSession)
		{
			dispatcher.dispatchEvent(source.getSession(), event, null);
		}
		if (event.isStop())
		{
			return;
		}
		if (targetsApplication)
		{
			dispatcher.dispatchEvent(source.getApplication(), event, null);
		}
	}

	/**
	 * Bubbles the event
	 * 
	 * @param event
	 *            event
	 */
	private void bubble(ComponentEvent<?> event)
	{
		IEventSink sink = event.getSink();

		boolean targetsComponent = sink instanceof Component;
		boolean targetsCycle = targetsComponent || sink instanceof RequestCycle;
		boolean targetsSession = targetsCycle || sink instanceof Session;
		boolean targetsApplication = targetsSession || sink instanceof Application;

		if (!targetsApplication && !targetsComponent)
		{
			dispatcher.dispatchEvent(sink, event, null);
			return;
		}

		if (targetsComponent)
		{
			Component cursor = (Component)sink;
			dispatchToComponent(dispatcher, cursor, event);
			if (event.isStop())
			{
				return;
			}
			cursor.visitParents(MarkupContainer.class, new ComponentEventVisitor(event, dispatcher));
		}

		if (event.isStop())
		{
			return;
		}
		if (targetsCycle)
		{
			dispatcher.dispatchEvent(source.getRequestCycle(), event, null);
		}
		if (event.isStop())
		{
			return;
		}
		if (targetsSession)
		{
			dispatcher.dispatchEvent(source.getSession(), event, null);
		}
		if (event.isStop())
		{
			return;
		}
		if (targetsApplication)
		{
			dispatcher.dispatchEvent(source.getApplication(), event, null);
		}
	}

	private static void dispatchToComponent(IEventDispatcher dispatcher, Component object,
		ComponentEvent<?> e)
	{
		dispatcher.dispatchEvent(object, e, null);

		if (e.isStop())
		{
			return;
		}

		List<? extends Behavior> behaviors = object.getBehaviors();
		for (Behavior behavior : behaviors)
		{
			dispatcher.dispatchEvent(behavior, e, object);
			if (e.isStop())
			{
				break;
			}
		}
	}


	/**
	 * Visitor used to broadcast events to components
	 * 
	 * @author igor
	 */
	private static class ComponentEventVisitor<T extends Component> implements IVisitor<T, Void>
	{
		private final ComponentEvent<?> e;
		private final IEventDispatcher dispatcher;

		/**
		 * Constructor
		 * 
		 * @param event
		 *            event to send
		 * @param dispatcher
		 *            event dispatcher
		 */
		private ComponentEventVisitor(ComponentEvent<?> event, IEventDispatcher dispatcher)
		{
			e = event;
			this.dispatcher = dispatcher;
		}

		/** {@inheritDoc} */
		@Override
		public void component(T object, IVisit<Void> visit)
		{
			dispatchToComponent(dispatcher, object, e);

			if (e.isStop())
			{
				visit.stop();
			}

			if (e.isShallow())
			{
				visit.dontGoDeeper();
			}

			e.resetShallow(); // reset shallow bit
		}
	}

}
