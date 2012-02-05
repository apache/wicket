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
package org.apache.wicket.extensions.breadcrumb.panel;

import org.apache.wicket.extensions.breadcrumb.BreadCrumbLink;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant;
import org.apache.wicket.util.lang.Args;

/**
 * Bread crumb link specifically for {@link BreadCrumbPanel bread crumb panels}. It uses a
 * {@link IBreadCrumbPanelFactory bread crumb factory} to function.
 * 
 * @author Eelco Hillenius
 */
public class BreadCrumbPanelLink extends BreadCrumbLink
{
	private static final long serialVersionUID = 1L;

	/** The bread crumb model. */
	private final IBreadCrumbModel breadCrumbModel;

	/** factory for creating bread crumbs panels. */
	private final IBreadCrumbPanelFactory breadCrumbPanelFactory;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param caller
	 *            The calling panel which will be used to get the {@link IBreadCrumbModel bread
	 *            crumb model} from.
	 * @param panelClass
	 *            The class to use for creating instances. Must be of type {@link BreadCrumbPanel},
	 *            and must have constructor
	 *            {@link BreadCrumbPanel#BreadCrumbPanel(String, IBreadCrumbModel)}
	 */
	public BreadCrumbPanelLink(final String id, final BreadCrumbPanel caller,
		final Class<? extends BreadCrumbPanel> panelClass)
	{
		this(id, caller.getBreadCrumbModel(), new BreadCrumbPanelFactory(panelClass));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param breadCrumbModel
	 *            The bread crumb model
	 * @param panelClass
	 *            The class to use for creating instances. Must be of type {@link BreadCrumbPanel},
	 *            and must have constructor
	 *            {@link BreadCrumbPanel#BreadCrumbPanel(String, IBreadCrumbModel)}
	 */
	public BreadCrumbPanelLink(final String id, final IBreadCrumbModel breadCrumbModel,
		final Class<? extends BreadCrumbPanel> panelClass)
	{
		this(id, breadCrumbModel, new BreadCrumbPanelFactory(panelClass));
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param breadCrumbModel
	 *            The bread crumb model
	 * @param breadCrumbPanelFactory
	 *            The factory to create bread crumb panels
	 */
	public BreadCrumbPanelLink(final String id, final IBreadCrumbModel breadCrumbModel,
		final IBreadCrumbPanelFactory breadCrumbPanelFactory)
	{
		super(id, breadCrumbModel);

		Args.notNull(breadCrumbModel, "breadCrumbModel");
		Args.notNull(breadCrumbPanelFactory, "breadCrumbPanelFactory");

		this.breadCrumbModel = breadCrumbModel;
		this.breadCrumbPanelFactory = breadCrumbPanelFactory;
	}

	/**
	 * Uses the set factory for creating a new instance of {@link IBreadCrumbParticipant}.
	 * 
	 * @see org.apache.wicket.extensions.breadcrumb.BreadCrumbLink#getParticipant(java.lang.String)
	 */
	@Override
	protected final IBreadCrumbParticipant getParticipant(final String componentId)
	{
		return breadCrumbPanelFactory.create(componentId, breadCrumbModel);
	}
}
