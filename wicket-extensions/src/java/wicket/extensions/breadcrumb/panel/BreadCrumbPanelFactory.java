/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.breadcrumb.panel;

import java.lang.reflect.Constructor;

import wicket.extensions.breadcrumb.IBreadCrumbModel;

/**
 * Simple factory that creates instances of a
 * {@link BreadCrumbPanel bread crumb panel} based on the class it is
 * constructed with.
 * 
 * @author Eelco Hillenius
 */
public final class BreadCrumbPanelFactory implements IBreadCrumbPanelFactory
{
	private static final long serialVersionUID = 1L;

	/** Class to construct. */
	private Class panelClass;

	/**
	 * Construct.
	 * 
	 * @param panelClass
	 *            The class to use for creating instances. Must be of type
	 *            {@link BreadCrumbPanel}, and must have constructor
	 *            {@link BreadCrumbPanel#BreadCrumbPanel(String, IBreadCrumbModel)}
	 */
	public BreadCrumbPanelFactory(final Class panelClass)
	{
		if (panelClass == null)
		{
			throw new IllegalArgumentException("argument panelClass must be not null");
		}

		if (!BreadCrumbPanel.class.isAssignableFrom(panelClass))
		{
			throw new IllegalArgumentException("argument panelClass (" + panelClass
					+ ") must extend class " + BreadCrumbPanel.class.getName());
		}


		this.panelClass = panelClass;

		// check whether it has the proper constructor
		getConstructor();
	}

	/**
	 * @see wicket.extensions.breadcrumb.panel.IBreadCrumbPanelFactory#create(java.lang.String,
	 *      wicket.extensions.breadcrumb.IBreadCrumbModel)
	 */
	public final BreadCrumbPanel create(String componentId, IBreadCrumbModel breadCrumbModel)
	{
		Constructor ctor = getConstructor();
		try
		{
			return (BreadCrumbPanel)ctor.newInstance(new Object[] { componentId, breadCrumbModel });
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
	private final Constructor getConstructor()
	{
		try
		{
			Constructor ctor = panelClass.getConstructor(new Class[] { String.class,
					IBreadCrumbModel.class });
			return ctor;
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
