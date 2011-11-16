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
package org.apache.wicket.markup.renderStrategy;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.util.visit.Visit;

/**
 * 
 * @author Juergen Donnerstag
 */
// TODO Wicket 1.6 :
// - move to o.a.w.util.visit because this is a useful visitor impl
// - relax its generics, it could be: DeepChildFirstVisitor<R> implements IVisitor<Component, R>
public abstract class DeepChildFirstVisitor implements IVisitor<Component, Void>
{
	/**
	 * Construct.
	 */
	public DeepChildFirstVisitor()
	{
	}

	/**
	 * Render the child hierarchy headers.
	 * 
	 * @param rootComponent
	 * @return The object return by component()
	 */
	public final Visit<Void> visit(final Component rootComponent)
	{
		Visit<Void> visitor = new Visit<Void>();
		return visit(rootComponent, visitor);
	}

	/**
	 * Render the child hierarchy headers.
	 * 
	 * @param rootComponent
	 * @param visit
	 * @return The object return by component()
	 */
	public final Visit<Void> visit(final Component rootComponent, final Visit<Void> visit)
	{
		Args.notNull(rootComponent, "rootComponent");
		Args.notNull(visit, "visit");

		// Component's don't have children; only MarkupContainers do
		if (!(rootComponent instanceof MarkupContainer))
		{
			// Call the visitor's callback method
			component(rootComponent, visit);
			return visit;
		}

		// while walking down, towards the deep child, we validate if the component is visible. If
		// not, there is no need to go any deeper
		if (preCheck(rootComponent) == false)
		{
			return visit;
		}

		if (visit.isContinue())
		{
			// Iterate over all children
			for (Component child : (MarkupContainer)rootComponent)
			{
				// visit the child
				visit(child, visit);
				if (visit.isStopped())
				{
					return visit;
				}
			}
		}

		// visit "this"
		component(rootComponent, visit);
		return visit;
	}

	@Override
	public abstract void component(Component component, IVisit<Void> visit);

	/**
	 * In order to find the deepest component, we traverse downwards starting from the root (e.g.
	 * Page). However, once a component is not disabled (preCheck() returns false), iteration will
	 * stop and traversal continues with the sibling.
	 * 
	 * @param component
	 *            The component to be tested
	 * @return True, if component is enabled
	 */
	public abstract boolean preCheck(Component component);
}
