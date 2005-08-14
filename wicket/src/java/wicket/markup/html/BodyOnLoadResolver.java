/*
 * $Id: WicketTagComponentResolver.java,v 1.4 2005/01/18 08:04:29 jonathanlocke
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
package wicket.markup.html;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.IComponentResolver;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;

/**
 * This is a tag resolver which handles &lt;body onLoad=".."&gt; tags. 
 * 
 * @author Juergen Donnerstag
 */
public class BodyOnLoadResolver implements IComponentResolver
{
	/** Logging */
	private static Log log = LogFactory.getLog(BodyOnLoadResolver.class);

	/**
	 * Try to resolve the tag, then create a component, add it to the container
	 * and render it.
	 * 
	 * @see wicket.IComponentResolver#resolve(MarkupContainer, MarkupStream,
	 *      ComponentTag)
	 * 
	 * @param container
	 *            The container parsing its markup
	 * @param markupStream
	 *            The current markupStream
	 * @param tag
	 *            The current component tag while parsing the markup
	 * @return true, if componentId was handle by the resolver. False, otherwise
	 */
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
		// It must be <body onLoad>
		if ((tag instanceof ComponentTag) && "body".equalsIgnoreCase(tag.getName()) 
		        && (tag.getNamespace() == null))
		{
			// Create, add and render the component.
		    
			// There is only one BodyOnLoadContainer allowed. That is we
			// don't have to worry about creating a unique id.
			BodyOnLoadContainer body = new BodyOnLoadContainer(tag.getId());
			container.autoAdd(body);

			// Yes, we handled the tag
			return true;
		}

		// We were not able to handle the tag
		return false;
	}
}