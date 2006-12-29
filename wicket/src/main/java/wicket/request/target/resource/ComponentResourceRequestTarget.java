/*
 * $Id: ComponentResourceRequestTarget.java,v 1.7 2006/02/12 20:25:40 eelco12
 * Exp $ $Revision$ $Date$
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
package wicket.request.target.resource;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.Component;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.RequestCycle;
import wicket.RequestListenerInterface;
import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebResponse;

/**
 * An implemenation of IRequestTarget that is used for the IResourceListener
 * event request.
 * 
 * @author jcompagner
 */
public final class ComponentResourceRequestTarget implements IRequestTarget
{
	private static final Logger log = LoggerFactory.getLogger(ComponentResourceRequestTarget.class);

	private final Page page;
	private final Component component;
	private final RequestListenerInterface listener;

	/**
	 * Construct.
	 * 
	 * @param page
	 * @param component
	 * @param listener
	 */
	public ComponentResourceRequestTarget(Page page, Component component,
			RequestListenerInterface listener)
	{
		this.page = page;
		this.component = component;
		this.listener = listener;
	}

	/**
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		page.beforeCallComponent(component, listener);

		try
		{
			// Invoke the interface method on the component
			listener.getMethod().invoke(component, new Object[] {});
		}
		catch (Exception e)
		{
			Response response = requestCycle.getResponse();
			if (response instanceof WebResponse)
			{
				((WebResponse)response).getHttpServletResponse().setStatus(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				log.error("error handling resource request for component " + component
						+ ", on page " + page + ", listener " + listener.getName() + " - "
						+ e.getMessage(), e);
				return;
			}
			else
			{
				throw new WicketRuntimeException("method " + listener.getName() + " of "
						+ listener.getMethod().getDeclaringClass() + " targetted at component "
						+ component + " threw an exception", e);
			}
		}
		finally
		{
			page.afterCallComponent(component, listener);
		}
	}

	/**
	 * @see wicket.IRequestTarget#detach(wicket.RequestCycle)
	 */
	public void detach(RequestCycle requestCycle)
	{
		page.internalDetach();
	}

	/**
	 * @see wicket.IRequestTarget#getLock(RequestCycle)
	 */
	public Object getLock(RequestCycle requestCycle)
	{
		return requestCycle.getSession();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof ComponentResourceRequestTarget)
		{
			ComponentResourceRequestTarget that = (ComponentResourceRequestTarget)obj;
			return page.equals(that.page) && component.equals(that.component);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int result = getClass().hashCode();
		result += page.hashCode();
		result += component.hashCode();
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder(getClass().getName()).append("@").append(hashCode())
				.append(page).append("->").append(component.getId()).append("->IResourceListener");
		return b.toString();
	}
}
