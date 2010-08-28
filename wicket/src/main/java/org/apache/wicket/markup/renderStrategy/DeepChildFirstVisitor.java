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
public abstract class DeepChildFirstVisitor implements IVisitor<Component, Component>
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
	public final Object visit(final Component rootComponent)
	{
		Args.notNull(rootComponent, "rootComponent");

		if (rootComponent instanceof MarkupContainer)
		{
			final Visit<Component> visit = new Visit<Component>();
			final Component[] lastComponent = new Component[1];
			Object rtn = ((MarkupContainer)rootComponent).visitChildren(new IVisitor<Component, Component>()
			{
				public void component(final Component component, final IVisit<Component> visit)
				{
					// skip invisible components
					if (component.isVisibleInHierarchy())
					{
						// In case it is a 'leaf' component, than ...
						if (!(component instanceof MarkupContainer) ||
							((MarkupContainer)component).size() == 0)
						{
							// Lets assume we rendered the 1st leaf already and we now reached
							// the 2nd leaf. If the 2nd leave has the very same parent, than we
							// don't do anything. If not, than we need to render the 1st component's
							// parents until such a parent is equal to the 2nd component parent.
							if (lastComponent[0] != null)
							{
								MarkupContainer parent = lastComponent[0].getParent();
								while ((parent != null) && (parent != rootComponent) &&
									isCommonParent(parent, lastComponent[0], component) == false)
								{
									// Render the container since all its children have been
									// rendered by now
									component(parent, visit);

									// If visitor returns a non-null value, it halts the traversal
									if (((Visit)visit).isStopped())
									{
										return;
									}

									parent = parent.getParent();
								}
							}

							// The 'leafs' header
							component(component, visit);

							// If visitor returns a non-null value, it halts the traversal
							if (((Visit)visit).isStopped())
							{
								return;
							}

							// Remember the current leaf, we need it for comparison later on
							visit.stop(component);
						}
					}
					else
					{
						// Remember the current leaf, we need it for comparison later on
						lastComponent[0] = component;
						visit.dontGoDeeper();
					}
				}

				/**
				 * 
				 * @param parent
				 * @param lastComponent
				 * @param currentComponent
				 * @return true, if parent is a common parent of both components
				 */
				private boolean isCommonParent(final MarkupContainer parent,
					final Component lastComponent, final Component currentComponent)
				{
					MarkupContainer p = lastComponent.getParent();
					while ((p != null) && (p != rootComponent) && (p != parent))
					{
						p = p.getParent();
					}

					if (p == parent)
					{
						p = currentComponent.getParent();
						while ((p != null) && (p != rootComponent) && (p != parent))
						{
							p = p.getParent();
						}

						if (p == parent)
						{
							return true;
						}
					}

					return false;
				}
			});

			// We still need to render the remaining containers
			if (lastComponent[0] != null)
			{
				MarkupContainer parent = lastComponent[0].getParent();
				while ((parent != null) && (parent != rootComponent))
				{
					// Render the container since all its children have been
					// rendered by now
					component(parent, visit);

					// If visitor returns a non-null value, it halts the traversal
					if (visit.isStopped())
					{
						return rtn;
					}

					parent = parent.getParent();
				}
			}

			return rtn;
		}

		return null;
	}

	/**
	 * @see org.apache.wicket.IVisitor#component(org.apache.wicket.Component)
	 */
	public abstract void component(Component component, IVisit<Component> visit);
}
