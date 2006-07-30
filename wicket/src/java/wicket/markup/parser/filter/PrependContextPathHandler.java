/*
 * $Id: PrependContextPathHandler.java 5082 2006-03-22 12:41:36 +0000 (Wed, 22
 * Mar 2006) joco01 $ $Revision$ $Date: 2006-03-22 12:41:36 +0000 (Wed,
 * 22 Mar 2006) $
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
package wicket.markup.parser.filter;

import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.parser.AbstractMarkupFilter;

/**
 * This is a markup inline filter which by default is added to the list of
 * markup filters. It can be added by means of subclassing
 * Application.newMarkupParser() like
 * 
 * <pre>
 *        public class MyApplication extends Application
 *        {
 *            ...
 *            public IMarkupFilter[] getAdditionalMarkupHandler()
 *            {
 *                return new IMarkupFilter[] { new new PrependContextPathHandler() };
 *            }
 * </pre>
 * 
 * The purpose of the filter is to prepend the web apps context path to all href
 * and src attributes found in the markup which contain a relative URL like
 * "myDir/myPage.gif". It is applied to all non wicket component tags
 * (attributes).
 * 
 * @author Juergen Donnerstag
 */
public final class PrependContextPathHandler extends AbstractMarkupFilter
{
	/** Logging */
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(PrependContextPathHandler.class);

	/** List of attribute names considered */
	private static final String attributeNames[] = new String[] { "href", "src" };

	private final Application application;

	/**
	 * This constructor will get the context path from the application settings.
	 * When it is not set the context path will be automatically resolved. This
	 * should work in most cases, and support the following clustering scheme
	 * 
	 * <pre>
	 *       node1.mydomain.com[/appcontext]
	 *       node2.mydomain.com[/appcontext]
	 *       node3.mydomain.com[/appcontext]
	 * </pre>
	 * 
	 * If it is set then you can map to other context like in clusters
	 * 
	 * <pre>
	 *       node1.mydomain.com/mycontext1/
	 *       node2.mydomain.com/mycontext2/
	 *       node3.mydomain.com/mycontext3/
	 *       mydomain.com/mycontext (load balancer)
	 * </pre>
	 * 
	 * or as a virtual server (app server and webserver)
	 * 
	 * <pre>
	 *       appserver.com/context mapped to webserver/ (context path should be '/')
	 * </pre>
	 * 
	 * @param application
	 *            The application object
	 * 
	 */
	public PrependContextPathHandler(final Application application)
	{
		this.application = application;
	}

	/**
	 * Get the next MarkupElement from the parent MarkupFilter and handle it if
	 * the specific filter criteria are met. Depending on the filter, it may
	 * return the MarkupElement unchanged, modified or it remove by asking the
	 * parent handler for the next tag.
	 * 
	 * @see wicket.markup.parser.IMarkupFilter#nextTag()
	 * @return Return the next eligible MarkupElement
	 */
	public MarkupElement nextTag() throws ParseException
	{
		// Get the next tag. If null, no more tags are available
		final ComponentTag tag = nextComponentTag();
		if (tag == null || tag.getId() != null)
		{
			return tag;
		}

		// Don't touch any wicket:id component
		if (tag.getId() != null)
		{
			return tag;
		}

		// this call should always get the default of the application or the
		// overriden one.
		String contextPath = application.getApplicationSettings().getContextPath();
		if (contextPath == null)
		{
			contextPath = "";
		}
		else if (contextPath.endsWith("/") == false)
		{
			contextPath += "/";
		}

		if (contextPath.length() > 0)
		{
			for (final String attrName : attributeNames)
			{
				final String attrValue = tag.getAttributes().getString(attrName);
				if ((attrValue != null) && (attrValue.startsWith("/") == false)
						&& (attrValue.indexOf(":") < 0) && !(attrValue.startsWith("#")))
				{
					final String url = contextPath + attrValue;
					tag.getAttributes().put(attrName, url);
					tag.setModified(true);
				}
			}
		}

		return tag;
	}
}
