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
package wicket.extensions.breadcrumb.panel;

import wicket.MarkupContainer;
import wicket.extensions.breadcrumb.BreadCrumbLink;
import wicket.extensions.breadcrumb.IBreadCrumbModel;
import wicket.extensions.breadcrumb.IBreadCrumbParticipant;

/**
 * Bread crumb link specifically for {@link BreadCrumbPanel bread crumb panels}.
 * It uses a {@link IBreadCrumbPanelFactory bread crumb factory} to function.
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
	 * @param parent
	 *            The parent component
	 * 
	 * @param id
	 *            The component id
	 * @param caller
	 *            The calling panel which will be used to get the
	 *            {@link IBreadCrumbModel bread crumb model} from.
	 * @param panelClass
	 *            The class to use for creating instances. Must be of type
	 *            {@link BreadCrumbPanel}, and must have constructor
	 *            {@link BreadCrumbPanel#BreadCrumbPanel(MarkupContainer, String, IBreadCrumbModel)}
	 */
	public BreadCrumbPanelLink(MarkupContainer parent, final String id,
			final BreadCrumbPanel caller, final Class panelClass)
	{
		this(parent, id, caller.getBreadCrumbModel(), new BreadCrumbPanelFactory(panelClass));
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * 
	 * @param id
	 *            The component id
	 * @param breadCrumbModel
	 *            The bread crumb model
	 * @param panelClass
	 *            The class to use for creating instances. Must be of type
	 *            {@link BreadCrumbPanel}, and must have constructor
	 *            {@link BreadCrumbPanel#BreadCrumbPanel(MarkupContainer, String, IBreadCrumbModel)}
	 */
	public BreadCrumbPanelLink(MarkupContainer parent, final String id,
			final IBreadCrumbModel breadCrumbModel, final Class panelClass)
	{
		this(parent, id, breadCrumbModel, new BreadCrumbPanelFactory(panelClass));
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent component
	 * 
	 * @param id
	 *            The component id
	 * @param breadCrumbModel
	 *            The bread crumb model
	 * @param breadCrumbPanelFactory
	 *            The factory to create bread crumb panels
	 */
	public BreadCrumbPanelLink(MarkupContainer parent, final String id,
			final IBreadCrumbModel breadCrumbModel,
			final IBreadCrumbPanelFactory breadCrumbPanelFactory)
	{
		super(parent, id, breadCrumbModel);

		if (breadCrumbModel == null)
		{
			throw new IllegalArgumentException("argument breadCrumbModel must be not null");
		}
		if (breadCrumbPanelFactory == null)
		{
			throw new IllegalArgumentException("argument breadCrumbPanelFactory must be not null");
		}

		this.breadCrumbModel = breadCrumbModel;
		this.breadCrumbPanelFactory = breadCrumbPanelFactory;
	}

	/**
	 * Uses the set factory for creating a new instance of
	 * {@link IBreadCrumbParticipant}.
	 * 
	 * @see wicket.extensions.breadcrumb.BreadCrumbLink#getParticipant(MarkupContainer,
	 *      java.lang.String)
	 */
	@Override
	protected final IBreadCrumbParticipant getParticipant(MarkupContainer parent, String componentId)
	{
		return breadCrumbPanelFactory.create(parent, componentId, breadCrumbModel);
	}
}
