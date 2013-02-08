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
package org.apache.wicket.devutils.stateless;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.string.StringList;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * Stateless checker. Checks if components with {@link StatelessComponent} annotation are really
 * stateless. This is a utility that is intended for use primarily during development. If you add an
 * instance of this class to your application, it will check all components or pages marked with the
 * <tt>StatelessComponent</tt> annotation to make sure that they are stateless as you intended.
 * 
 * This is useful when trying to maintain stateless pages since it is very easy to inadvertently add
 * a component to a page that internally uses stateful links, etc.
 * 
 * @author Marat Radchenko
 * @see StatelessComponent
 */
public class StatelessChecker implements IComponentOnBeforeRenderListener
{
	/**
	 * Returns <code>true</code> if checker must check given component, <code>false</code>
	 * otherwise.
	 * 
	 * @param component
	 *            component to check.
	 * @return <code>true</code> if checker must check given component.
	 */
	protected boolean mustCheck(final Component component)
	{
		final StatelessComponent ann = component.getClass().getAnnotation(StatelessComponent.class);
		return (ann != null) && ann.enabled();
	}

	/**
	 * @see org.apache.wicket.application.IComponentOnBeforeRenderListener#onBeforeRender(org.apache.wicket.Component)
	 */
	@Override
	public void onBeforeRender(final Component component)
	{
		if (mustCheck(component))
		{
			final IVisitor<Component, Component> visitor = new IVisitor<Component, Component>()
			{
				@Override
				public void component(final Component comp, final IVisit<Component> visit)
				{
					if ((component instanceof Page) && mustCheck(comp))
					{
						// Do not go deeper, because this component will be
						// checked by checker
						// itself.
						// Actually we could go deeper but that would mean we
						// traverse it twice
						// (for current component and for inspected one).
						// We go deeper for Page because full tree will be
						// inspected during
						// isPageStateless call.
						visit.dontGoDeeper();
					}
					else if (!comp.isStateless())
					{
						visit.stop(comp);
					}
					else
					{
						// continue
					}
				}
			};

			final String msg = "'" + component + "' claims to be stateless but isn't.";
			if (component.isStateless() == false)
			{
				StringList statefulBehaviors = new StringList();
				for (Behavior b : component.getBehaviors())
				{
					if (b.getStatelessHint(component) == false)
					{
						statefulBehaviors.add(Classes.name(b.getClass()));
					}
				}
				String reason;
				if (statefulBehaviors.size() == 0)
				{
				    reason = " Possible reason: no stateless hint";
				}
				else
				{
				    reason = " Stateful behaviors: " + statefulBehaviors.join();
				}
				throw new IllegalStateException(msg + reason);
			}

			if (component instanceof MarkupContainer)
			{
				// Traverse children
				final Object o = ((MarkupContainer)component).visitChildren(visitor);
				if (o != null)
				{
					throw new IllegalArgumentException(msg + " Offending component: " + o);
				}
			}

			if (component instanceof Page)
			{
				final Page p = (Page)component;
				if (!p.isBookmarkable())
				{
					throw new IllegalArgumentException(msg +
						" Only bookmarkable pages can be stateless");
				}
				if (!p.isPageStateless())
				{
					throw new IllegalArgumentException(msg + " for unknown reason");
				}
			}
		}
	}
}
