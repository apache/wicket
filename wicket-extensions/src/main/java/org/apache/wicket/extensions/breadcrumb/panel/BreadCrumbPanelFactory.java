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

import java.lang.reflect.Constructor;

import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.util.lang.Args;


/**
 * Simple factory that creates instances of a {@link BreadCrumbPanel bread crumb panel} based on the
 * class it is constructed with.
 * 
 * @author Eelco Hillenius
 */
public final class BreadCrumbPanelFactory implements IBreadCrumbPanelFactory
{
	private static final long serialVersionUID = 1L;

	/** Class to construct. */
	private final Class<? extends BreadCrumbPanel> panelClass;

	/**
	 * Construct.
	 * 
	 * @param panelClass
	 *            The class to use for creating instances. Must be of type {@link BreadCrumbPanel},
	 *            and must have constructor
	 *            {@link BreadCrumbPanel#BreadCrumbPanel(String, IBreadCrumbModel)}
	 */
	public BreadCrumbPanelFactory(final Class<? extends BreadCrumbPanel> panelClass)
	{
		Args.notNull(panelClass, "panelClass");

		if (!BreadCrumbPanel.class.isAssignableFrom(panelClass))
		{
			throw new IllegalArgumentException("argument panelClass (" + panelClass +
				") must extend class " + BreadCrumbPanel.class.getName());
		}


		this.panelClass = panelClass;

		// check whether it has the proper constructor
		getConstructor();
	}

	/**
	 * @see org.apache.wicket.extensions.breadcrumb.panel.IBreadCrumbPanelFactory#create(java.lang.String,
	 *      org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel)
	 */
	@Override
	public final BreadCrumbPanel create(final String componentId,
		final IBreadCrumbModel breadCrumbModel)
	{
		Constructor<? extends BreadCrumbPanel> ctor = getConstructor();
		try
		{
			return ctor.newInstance(componentId, breadCrumbModel);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the proper constructor of the panel class.
	 * 
	 * @return The constructor.
	 */
	private Constructor<? extends BreadCrumbPanel> getConstructor()
	{
		try
		{
			return panelClass.getConstructor(String.class, IBreadCrumbModel.class);
		}
		catch (SecurityException e)
		{
			throw new RuntimeException(e);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
}
