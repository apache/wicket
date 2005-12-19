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
package wicket.markup.parser.filter;

import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.parser.AbstractMarkupFilter;
import wicket.protocol.http.WebRequestCycle;

/**
 * This is a markup inline filter which by default is not added to the list of
 * markup filters. It can be added by means of subclassing
 * Application.newMarkupParser() like
 * 
 * <pre>
 *     public class MyApplication extends Application
 *     {
 *         ...
 *         public IMarkupFilter[] getAdditionalMarkupHandler()
 *         {
 *             return new IMarkupFilter[] { new new PrependContextPathHandler() };
 *         }
 * </pre>
 * 
 * The purpose of the filter is to prepend the web apps context path to all href
 * and src attributes found in the markup which contain a relative URL like
 * "myDir/myPage.gif". It is applied to all tags (attributes) no matter whether
 * the tag is a wicket tag or not.
 * 
 * @author Juergen Donnerstag
 */
public final class PrependContextPathHandler extends AbstractMarkupFilter
{
	/** Logging */
	private static final Log log = LogFactory.getLog(PrependContextPathHandler.class);

	/** List of attribute names considered */
	private static final String attributeNames[] = new String[] { "href", "src" };

	/** The application's servlet context path */
	private String contextPath;

	/**
	 * Construct. This constructor will automatically resolve the context path.
	 * This should work in most cases, and support the following clustering
	 * scheme
	 * 
	 * <pre>
	 *    node1.mydomain.com
	 *    node2.mydomain.com
	 *    node3.mydomain.com
	 * </pre>
	 * 
	 * 
	 */
	public PrependContextPathHandler()
	{
		super(null);
	}

	/**
	 * Construct. In order to support cluster envs like
	 * 
	 * <pre>
	 *    node1.mydomain.com/mycontext1/
	 *    node2.mydomain.com/mycontext2/
	 *    node3.mydomain.com/mycontext3/
	 *    mydomain.com/mycontext (load balancer)
	 * </pre>
	 * 
	 * This kind of setup requires the user to specify the context path of the
	 * load balancer so that the path is targetted for the load balancer and not
	 * this cluster node.
	 * 
	 * 
	 * @param virtualContextPath
	 *            The path to be used instead of the real context path
	 */
	public PrependContextPathHandler(final String virtualContextPath)
	{
		super(null);

		this.contextPath = virtualContextPath;
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
		final ComponentTag tag = (ComponentTag)getParent().nextTag();
		if (tag == null)
		{
			return tag;
		}

		// A new handler is created for each markup file. Hence it is
		// sufficient to "create" the context path just once.
		if (contextPath == null)
		{
			contextPath = ((WebRequestCycle)RequestCycle.get()).getWebRequest().getContextPath();
			if (contextPath == null)
			{
				contextPath = "";
			}
			else if (contextPath.endsWith("/") == false)
			{
				contextPath += "/";
			}
		}

		if (contextPath.length() > 0)
		{
			// Modify all relevant attributes
			for (int i = 0; i < attributeNames.length; i++)
			{
				String attrName = attributeNames[i];
				String attrValue = tag.getAttributes().getString(attrName);
				if ((attrValue != null) && (attrValue.startsWith("/") == false)
						&& (attrValue.indexOf(":") < 0))
				{
					String url = contextPath + attrValue;
					tag.getAttributes().put(attrName, url);
					tag.setModified(true);
				}
			}
		}

		return tag;
	}
}
