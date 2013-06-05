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
package org.apache.wicket.markup.html.basic;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.lang.Args;


/**
 * <code>&lt;wicket:enclosure&gt;</code> is nice and prevents that users have to add boilerplate to
 * their application. But it is not without problems. The child components are children in the
 * markup, but the auto-component generated for the enclosure tag will not magically re-parent the
 * child components. Thus the markup hierarchy and the component hierarchy will be out of sync. The
 * automatically created enclosure container will be created along side its "children" with both
 * attached to the very same parent container. That leads to a tricky situation since e.g.
 * <code>onBeforeRender()</code> will be called for enclosure children even if the enclosure is made
 * invisible by it controlling child.
 * <p>
 * On top auto-components cannot keep any state. A new instance is created during each render
 * process and automatically deleted at the end. That implies that we cannot prevent
 * <code>validation()</code> from being called, since validation() is called before the actual
 * render process has started.
 * </p>
 * <p>
 * Where any of these problems apply, you may replace the tag and manually add this simple container
 * which basically does the same. But instead of adding the children to the Page, Panel whatever,
 * you must add the children to this container in order to keep the component hierarchy in sync.
 * </p>
 * 
 * @author Juergen Donnerstag
 * @since 1.5
 */
public class EnclosureContainer extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	/** The child component to delegate the isVisible() call to */
	private final Component child;

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param child
	 *            child component that will control the visibility of the enclosure
	 */
	public EnclosureContainer(final String id, final Component child)
	{
		super(id);

		Args.notNull(child, "child");

		this.child = child;

		// Usually we don't want this extra tag
		setRenderBodyOnly(true);
	}

	/**
	 * Overriden to set the visibility depending on childs {@link #determineVisibility()}.
	 */
	@Override
	protected void onConfigure()
	{
		child.configure();

		setVisible(child.determineVisibility());
	}
}
