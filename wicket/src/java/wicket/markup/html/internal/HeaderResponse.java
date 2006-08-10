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
package wicket.markup.html.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import wicket.RequestCycle;
import wicket.Response;
import wicket.markup.html.IHeaderResponse;
import wicket.markup.html.PackageResourceReference;
import wicket.util.string.JavascriptUtils;

/**
 * Default implementation of the {@link IHeaderResponse} interface.
 * 
 * @author Matej Knopp
 */
class HeaderResponse implements IHeaderResponse
{
	private static final long serialVersionUID = 1L;

	private Response response;

	private Set<Object> rendered = new HashSet<Object>();

	/**
	 * Creates a new header response instance.
	 * 
	 * @param response
	 *            response used to write the head elements
	 */
	public HeaderResponse(Response response)
	{
		this.response = response;
	}

	/**
	 * @see wicket.markup.html.IHeaderResponse#markRendered(java.lang.Object)
	 */
	public final void markRendered(Object object)
	{
		rendered.add(object);
	}

	/**
	 * @see wicket.markup.html.IHeaderResponse#renderCSSReference(wicket.markup.html.PackageResourceReference)
	 */
	public final void renderCSSReference(PackageResourceReference reference)
	{
		if (wasRendered(reference) == false)
		{
			final CharSequence url = RequestCycle.get().urlFor(reference);
			response.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
			response.write(url);
			response.println("\"></link>");
			markRendered(reference);
		}
	}

	/**
	 * @see wicket.markup.html.IHeaderResponse#renderJavascriptReference(wicket.markup.html.PackageResourceReference)
	 */
	public final void renderJavascriptReference(PackageResourceReference reference)
	{
		if (wasRendered(reference) == false)
		{
			JavascriptUtils.writeJavascriptUrl(getResponse(), RequestCycle.get().urlFor(reference));
			markRendered(reference);
		}
	}

	/**
	 * @see wicket.markup.html.IHeaderResponse#renderJavascript(java.lang.CharSequence, java.lang.String)
	 */
	public void renderJavascript(CharSequence javascript, String id)
	{
		List<Object> token = Arrays.asList(new Object[] { javascript, id });
		if (wasRendered(token) == false) 
		{
			JavascriptUtils.writeJavascript(getResponse(), javascript, id);
			markRendered(token);
		}		
	}

	/**
	 * @see wicket.markup.html.IHeaderResponse#renderString(java.lang.CharSequence)
	 */
	public final void renderString(CharSequence string)
	{
		if (wasRendered(string) == false)
		{
			getResponse().write(string);
			markRendered(string);
		}
	}

	/**
	 * @see wicket.markup.html.IHeaderResponse#wasRendered(java.lang.Object)
	 */
	public final boolean wasRendered(Object object)
	{
		return rendered.contains(object);
	}

	/**
	 * @see wicket.markup.html.IHeaderResponse#getResponse()
	 */
	public final Response getResponse()
	{
		return response;
	}
}
