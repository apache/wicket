/*
 * $Id: WicketTagComponentResolver.java,v 1.4 2005/01/18 08:04:29 jonathanlocke
 * Exp $ $Revision$ $Date: 2005/12/27 10:38:52 $
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

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.resolver.IComponentResolver;

/**
 * 
 * @author svenmeier
 */
public class ComponentResolvingPage extends WebPage implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	/** onEndRequestWasCalledOnAutoAddedComponent */
	public boolean onEndRequestWasCalledOnAutoAddedComponent = false;

	/**
	 * Construct.
	 */
	public ComponentResolvingPage()
	{
	}

	/**
	 * 
	 * @see wicket.markup.resolver.IComponentResolver#resolve(wicket.MarkupContainer,
	 *      wicket.markup.MarkupStream, wicket.markup.ComponentTag)
	 */
	public boolean resolve(final MarkupContainer container, final MarkupStream markupStream,
			final ComponentTag tag)
	{
		if ("test".equals(tag.getId()))
		{
			autoAdd(new Label("test", "TEST")
			{
				private static final long serialVersionUID = 1L;

				protected void onEndRequest()
				{
					onEndRequestWasCalledOnAutoAddedComponent = true;
				}
			});
			return true;
		}
		return false;
	}
}