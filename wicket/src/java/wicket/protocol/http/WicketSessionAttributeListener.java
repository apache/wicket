/*
 * $Id: WicketSessionAtributeListener.java,v 1.5 2005/02/22 17:42:33
 * jonathanlocke Exp $ $Revision$ $Date$
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

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

import wicket.Session;

/**
 * If you want clustering to work in Wicket, you /must/ add this attribute
 * listener in your web.xml file like this:
 * <p>
 * &lt;listener&gt;
 *  &lt;listener-class&gt;wicket.protocol.http.WicketSessionAttributeListener&lt;/listener-class&gt;
 * &lt;/listener&gt;
 * 
 * @author Jonathan Locke
 */
public class WicketSessionAttributeListener implements HttpSessionAttributeListener
{
	/**
	 * @see HttpSessionAttributeListener#attributeAdded(HttpSessionBindingEvent)
	 */
	public void attributeAdded(HttpSessionBindingEvent event)
	{
		updateSession(event);
	}

	/**
	 * @see HttpSessionAttributeListener#attributeRemoved(HttpSessionBindingEvent)
	 */
	public void attributeRemoved(HttpSessionBindingEvent event)
	{
	}

	/**
	 * @see HttpSessionAttributeListener#attributeReplaced(HttpSessionBindingEvent)
	 */
	public void attributeReplaced(HttpSessionBindingEvent event)
	{
		updateSession(event);
	}

	/**
	 * Called when an attribute
	 * 
	 * @param event
	 *            The session binding event
	 */
	void updateSession(HttpSessionBindingEvent event)
	{
		final Object value = event.getValue();
		if (value instanceof Session)
		{
			((Session)value).updateSession();
		}
	}
}
