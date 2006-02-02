/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.ajax;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import wicket.Component;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.RequestCycle;
import wicket.Response;
import wicket.protocol.http.WebResponse;

/**
 * @author Igor Vaynberg (ivaynberg)
 */
public class AjaxRequestTarget implements IRequestTarget
{
	/** the component instances that will be rendered */
	private final List/* <Component> */components = new ArrayList();

	private final List/* <String> */javascripts = new ArrayList();

	/**
	 * Constructor
	 */
	public AjaxRequestTarget()
	{
	}

	/**
	 * Adds a component to the list of components to be rendered
	 * 
	 * @param component
	 *            component to be rendered
	 */
	public void addComponent(Component component)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("component cannot be null");
		}
		else if (component instanceof Page)
		{
			throw new IllegalArgumentException("component cannot be a page");
		}

		components.add(component);
	}

	/**
	 * Adds javascript that will be evaluated on the client side
	 * 
	 * @param javascript
	 */
	public void addJavascript(String javascript)
	{
		if (javascript == null)
		{
			throw new IllegalArgumentException("javascript cannot be null");
		}

		javascripts.add(javascript);
	}

	/**
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public void respond(final RequestCycle requestCycle)
	{
		WebResponse response = (WebResponse) requestCycle.getResponse();

		response.setContentType("text/xml");

		response.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT" );
		response.setHeader("Cache-Control", "no-cache, must-revalidate" );
		response.setHeader("Pragma", "no-cache" );

		response.write("<?xml version=\"1.0\"?>");

		response.write("<ajax-response>");

		Iterator it = components.iterator();
		while (it.hasNext())
		{
			Component c = (Component) it.next();
			respondComponent(response, c);
		}

		it = javascripts.iterator();
		while (it.hasNext())
		{
			String js = (String) it.next();
			respondInvocation(response, js);
		}
		response.write("</ajax-response>");
	}

	private void respondInvocation(Response response, String js)
	{
		response.write("<evaluate>");
		response.write("<![CDATA[");
		response.write(js);
		response.write("]]>");
		response.write("</evaluate>");

	}

	private void respondComponent(Response response, Component component)
	{
		String id;
		if (component.getMarkupAttributes().containsKey("id")) {
			id=component.getMarkupAttributes().getString("id");
		} else {
			id=component.getPageRelativePath();
		}
		
		response.write("<component id=\"" + id + "\">");

		response.write("<![CDATA[");

		// Initialize temporary variables
		Page page = component.getPage();
		if (page != null)
		{
			page.startComponentRender(component);
		}

		boolean old = component.getRenderBodyOnly();
		component.setRenderBodyOnly(true);

		// Render the component
		component.doRender();

		component.setRenderBodyOnly(old);

		if (page != null)
		{
			page.endComponentRender(component);
		}

		response.write("]]>");

		response.write("</component>");
	}

	/**
	 * @see wicket.IRequestTarget#cleanUp(wicket.RequestCycle)
	 */
	public void cleanUp(final RequestCycle requestCycle)
	{
	}

	/**
	 * @see wicket.IRequestTarget#getLock(RequestCycle)
	 */
	public Object getLock(final RequestCycle requestCycle)
	{
		return requestCycle.getSession();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(final Object obj)
	{
		if (obj instanceof AjaxRequestTarget)
		{
			AjaxRequestTarget that = (AjaxRequestTarget) obj;
			return components.equals(that.components)
					&& javascripts.equals(that.javascripts);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int result = "AjaxRequestTarget".hashCode();
		result += components.hashCode() * 17;
		result += javascripts.hashCode() * 17;
		return result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "AjaxRequestTarget@"
				+ hashCode() + " components {" + components + "} javascripts {"
				+ javascripts + "}";
	}
}