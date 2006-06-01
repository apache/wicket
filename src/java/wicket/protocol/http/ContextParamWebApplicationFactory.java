/*
 * $Id$ $Revision$ $Date$
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
package wicket.protocol.http;

import wicket.WicketRuntimeException;

/**
 * Factory that creates application objects based on the class name specified in
 * the APP_CLASS_PARAM context variable.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class ContextParamWebApplicationFactory implements IWebApplicationFactory
{
	/**
	 * context parameter name that must contain the class name of the
	 * application
	 */
	public final String APP_CLASS_PARAM = "applicationClassName";

	/** @see IWebApplicationFactory#createApplication(WicketServlet) */
	public WebApplication createApplication(WicketFilter filter)
	{
		final String applicationClassName = filter.getFilterConfig().getInitParameter(APP_CLASS_PARAM);

		if (applicationClassName == null)
		{
			throw new WicketRuntimeException(
					"servlet init param ["
							+ APP_CLASS_PARAM
							+ "] is missing. If you are trying to use your own implementation of IWebApplicationFactory and get this message then the servlet init param ["
							+ WicketFilter.APP_FACT_PARAM + "] is missing");
		}

		try
		{
			final Class applicationClass = getClass().getClassLoader().loadClass(
					applicationClassName);
			if (WebApplication.class.isAssignableFrom(applicationClass))
			{
				// Construct WebApplication subclass
				return (WebApplication)applicationClass.newInstance();
			}
			else
			{
				throw new WicketRuntimeException("Application class " + applicationClassName
						+ " must be a subclass of WebApplication");
			}
		}
		catch (ClassNotFoundException e)
		{
			throw new WicketRuntimeException("Unable to create application of class "
					+ applicationClassName, e);
		}
		catch (InstantiationException e)
		{
			throw new WicketRuntimeException("Unable to create application of class "
					+ applicationClassName, e);
		}
		catch (IllegalAccessException e)
		{
			throw new WicketRuntimeException("Unable to create application of class "
					+ applicationClassName, e);
		}
		catch (SecurityException e)
		{
			throw new WicketRuntimeException("Unable to create application of class "
					+ applicationClassName, e);
		}
	}
}