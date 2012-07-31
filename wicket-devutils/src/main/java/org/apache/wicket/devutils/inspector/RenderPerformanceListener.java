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
package org.apache.wicket.devutils.inspector;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.devutils.debugbar.DebugBar;
import org.apache.wicket.markup.html.debug.PageView;
import org.apache.wicket.util.lang.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener that adds a special {@link Behavior} that measures the time needed by a component to
 * render itself. {@link MarkupContainer}'s render includes the time for rendering its children so
 * the time accumulates.
 * <p>
 * To enable this listener use the following in YourApplication.init():
 * 
 * <pre>
 * getComponentInstantiationListeners().add(new RenderPerformanceListener());
 * </pre>
 * 
 * </p>
 */
public class RenderPerformanceListener implements IComponentInstantiationListener
{
	private static final Logger log = LoggerFactory.getLogger(RenderPerformanceListener.class);

	@Override
	public void onInstantiation(final Component component)
	{
		if (accepts(component))
		{
			component.add(new RenderMeasuringBehavior());
		}
	}

	/**
	 * Filters which components' render performance should be measured.
	 * 
	 * @param component
	 *            the component that is instantiated
	 * @return {@code true} if render time should be measured the for this component, {@code false}
	 *         - otherwise
	 */
	protected boolean accepts(final Component component)
	{
		return component.isAuto() == false;
	}

	/**
	 * A {@link Behavior} that sets the current time in the meta data of the component before its
	 * render starts and use it to calculate how long it took. The collected data is logged as debug
	 * and also can be read from the meta data with key {@link PageView#RENDER_KEY}.
	 * {@link DebugBar}'s inspector panel visualizes it.
	 */
	private static class RenderMeasuringBehavior extends Behavior
	{
		@Override
		public void beforeRender(final Component component)
		{
			super.beforeRender(component);
			if (component.isAuto() == false)
			{
				Long now = System.currentTimeMillis();
				component.setMetaData(PageView.RENDER_KEY, now);
			}
		}

		@Override
		public void afterRender(final Component component)
		{
			super.afterRender(component);
			Long renderEnd = System.currentTimeMillis();
			Long renderStart = component.getMetaData(PageView.RENDER_KEY);
			if (renderStart != null && component.isAuto() == false)
			{
				Long duration = renderEnd - renderStart;
				component.setMetaData(PageView.RENDER_KEY, duration);

				if (log.isDebugEnabled())
				{
					String componentPath = (component instanceof Page) ? Classes.simpleName(component.getClass())
							+ " page" : component.getPageRelativePath();
					log.debug("rendered '{}' for {}ms", componentPath, duration);
				}
			}
		}
	}
}
