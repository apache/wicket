/*
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
package wicket.protocol.http.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.AbortException;
import wicket.Application;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.protocol.http.ContextParamWebApplicationFactory;
import wicket.protocol.http.WebApplicationFactoryCreationException;

/**
 * Portlet class for all wicket portlet applications. The specific application
 * class to instantiate should be specified to the application server via an
 * portlet-preferences/preference argument named "applicationClassName" in the
 * portlet declaration, which is typically in a <i>portlet.xml </i> file.
 * 
 * <pre>
 *          	&lt;portlet id=&quot;WicketPortlet&quot;&gt;
 * 		&lt;portlet-name&gt;WicketPortlet&lt;/portlet-name&gt;
 * 		&lt;portlet-class&gt;
 * 			wicket.protocol.http.portlet.WicketPortlet
 * 		&lt;/portlet-class&gt;
 * 		&lt;expiration-cache&gt;0&lt;/expiration-cache&gt;
 * 		&lt;resource-bundle&gt;WicketPortlet&lt;/resource-bundle&gt;
 * 
 * 		&lt;portlet-preferences&gt;
 * 			&lt;preference&gt;
 * 				&lt;name&gt;applicationClassName&lt;/name&gt;
 *  				&lt;value&gt;wicket.examples.portlet.ExamplePortletApplication&lt;/value&gt;
 * 			&lt;/preference&gt;		
 * 		&lt;/portlet-preferences&gt;
 * 
 * 		&lt;supports&gt;
 * 			&lt;mime-type&gt;text/html&lt;/mime-type&gt;
 * 			&lt;portlet-mode&gt;VIEW&lt;/portlet-mode&gt;
 * 			&lt;portlet-mode&gt;EDIT&lt;/portlet-mode&gt;
 * 			&lt;portlet-mode&gt;HELP&lt;/portlet-mode&gt;
 * 		&lt;/supports&gt;
 * 		&lt;portlet-info&gt;
 * 			&lt;title&gt;WicketPortlet&lt;/title&gt;
 * 		&lt;/portlet-info&gt;
 * 	&lt;/portlet&gt;
 * </pre>
 * 
 * Note that the applicationClassName parameter you specify must be the fully
 * qualified name of a class that extends PortletApplication. If your class
 * cannot be found, does not extend PortletApplication or cannot be
 * instantiated, a runtime exception of type WicketRuntimeException will be
 * thrown.
 * </p>
 * As an alternative, you can configure an application factory instead. This
 * looks like:
 * 
 * <pre>
 *           
 * 			&lt;preference&gt;
 * 				&lt;name&gt;applicationFactoryClassName&lt;/name&gt;
 *  				&lt;value&gt;com.cemron.cpf.web.wicket.SpringApplicationFactory&lt;/value&gt;
 * 			&lt;/preference&gt;		
 * </pre>
 * 
 * and it has to satisfy interface
 * {@link wicket.protocol.http.portlet.IPortletApplicationFactory}.
 * 
 * In order to support frameworks like Spring, the class is non-final and the
 * variable portletApplication is protected instead of private. Thus subclasses
 * may provide there own means of providing the application object.
 * 
 * @see wicket.RequestCycle
 * @author Janne Hietam&auml;ki
 * 
 */
public class WicketPortlet extends GenericPortlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * The name of the context parameter that specifies application factory
	 * class
	 */
	public static final String APP_FACT_PARAM = "applicationFactoryClassName";

	/** Log. */
	private static final Log log = LogFactory.getLog(WicketPortlet.class);

	/** The application this portlet is serving */
	protected PortletApplication portletApplication = null;


	/*
	 * @see javax.portlet.GenericPortlet#destroy()
	 */
	public final void destroy()
	{
		if (this.portletApplication != null)
		{
			this.portletApplication.destroyPortlet();
			this.portletApplication = null;
		}
	}

	public final void init(PortletConfig cfg) throws PortletException
	{
		super.init(cfg);
		final IPortletApplicationFactory factory = getApplicationFactory();

		// Construct PortletApplication subclass
		this.portletApplication = factory.createApplication(this);

		// Set this WicketPortlet as the portlet for the portlet application
		this.portletApplication.setWicketPortlet(this);

		// Store instance of this application object in portlet context to make
		// integration with outside world easier
		final String contextKey = "wicket:" + getPortletConfig().getPortletName();
		getPortletContext().setAttribute(contextKey, this.portletApplication);

		// Finished
		log.info("WicketPortlet loaded application " + this.portletApplication.getName() + " via "
				+ factory.getClass().getName() + " factory");

		try
		{
			Application.set(portletApplication);
			this.portletApplication.initPortlet();

			// We initialize components here rather than in the constructor or
			// in the internal init, because in the init method class aliases
			// can be added, that would be used in installing resources in the
			// component.
			this.portletApplication.initializeComponents();
		}
		finally
		{
			Application.unset();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.portlet.GenericPortlet#processAction(javax.portlet.ActionRequest,
	 *      javax.portlet.ActionResponse)
	 */
	public final void processAction(ActionRequest req, ActionResponse res)
	{
		// First, set the webapplication for this thread
		Application.set(portletApplication);

		// Create a response object and set the output encoding according to
		// wicket's application setttings.
		final WicketPortletResponse response = portletApplication.newPortletResponse(res);

		// Create a new webrequest
		final WicketPortletRequest request = portletApplication.newPortletRequest(req);

		final WicketPortletSession session = portletApplication.getSession(request);

		try
		{
			// Create a new request cycle
			// FIXME post 1.2 Instead of doing this, we should get a request
			// cycle factory
			// from the application settings and use that. That way we are a
			// step
			// closer to a session-less operation of Wicket.
			PortletRequestCycle cycle = (PortletRequestCycle)session.newRequestCycle(request,
					response);
			try
			{
				// Process request
				cycle.request();
			}
			catch (AbortException e)
			{
				// noop
			}
		}
		finally
		{
			// Clean up thread local session
			Session.unset();

			// Clean up thread local application
			Application.unset();

		}
	}

	/*
	 * public void doView(RenderRequest req, RenderResponse res){
	 */
	public final void render(RenderRequest req, RenderResponse res) throws PortletException,
			IOException
	{
		// First, set the webapplication for this thread
		Application.set(portletApplication);

		// Create a response object and set the output encoding according to
		// wicket's application setttings.
		final WicketPortletResponse response = portletApplication.newPortletResponse(res);

		// Create a new webrequest
		final WicketPortletRequest request = portletApplication.newPortletRequest(req);

		final WicketPortletSession session = portletApplication.getSession(request);

		try
		{
			// Create a new request cycle
			// FIXME post 1.2 Instead of doing this, we should get a request
			// cycle factory
			// from the application settings and use that. That way we are a
			// step
			// closer to a session-less operation of Wicket.
			PortletRequestCycle cycle = (PortletRequestCycle)session.newRequestCycle(request,
					response);

			try
			{
				// Process request
				cycle.request();
			}
			catch (AbortException e)
			{
				// noop
			}
		}
		finally
		{
			// Clean up thread local session
			Session.unset();

			// Clean up thread local application
			Application.unset();

		}
	}

	/**
	 * Creates the web application factory instance.
	 * 
	 * If no APP_FACT_PARAM is specified in web.xml
	 * ContextParamWebApplicationFactory will be used by default.
	 * 
	 * @see ContextParamWebApplicationFactory
	 * 
	 * @return application factory instance
	 */
	protected final IPortletApplicationFactory getApplicationFactory()
	{
		final String appFactoryClassName = getPortletContext().getInitParameter(APP_FACT_PARAM);

		if (appFactoryClassName == null)
		{
			// If no context param was specified we return the default factory
			return new PortletPreferencesPortletApplicationFactory();
		}
		else
		{
			try
			{
				// Try to find the specified factory class
				final Class factoryClass = getClass().getClassLoader().loadClass(
						appFactoryClassName);

				// Instantiate the factory
				return (IPortletApplicationFactory)factoryClass.newInstance();
			}
			catch (ClassCastException e)
			{
				throw new WicketRuntimeException("Application factory class " + appFactoryClassName
						+ " must implement IPortletApplicationFactory");
			}
			catch (ClassNotFoundException e)
			{
				throw new WebApplicationFactoryCreationException(appFactoryClassName, e);
			}
			catch (InstantiationException e)
			{
				throw new WebApplicationFactoryCreationException(appFactoryClassName, e);
			}
			catch (IllegalAccessException e)
			{
				throw new WebApplicationFactoryCreationException(appFactoryClassName, e);
			}
			catch (SecurityException e)
			{
				throw new WebApplicationFactoryCreationException(appFactoryClassName, e);
			}
		}
	}
}