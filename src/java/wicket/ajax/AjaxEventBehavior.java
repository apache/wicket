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

import wicket.markup.ComponentTag;
import wicket.util.string.Strings;

/**
 * 
 * 
 */
public abstract class AjaxEventBehavior extends AjaxBehavior
{
	private static final long serialVersionUID = 1L;

	private String event;

	/**
	 * Construct.
	 * 
	 * @param event
	 */
	public AjaxEventBehavior(final String event)
	{
		if (Strings.isEmpty(event))
		{
			throw new IllegalArgumentException("argument [event] cannot be null or empty");
		}

		onCheckEvent(event);

		this.event = event;
	}

	/**
	 * 
	 * @see wicket.behavior.AbstractAjaxBehavior#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		String handler = getEventHandler();

		if (event.equalsIgnoreCase("href"))
		{
			// if we are using the href attr we need to prefix with
			// 'javascript:' and also make sure we catch any return value
			// otherwise the browser will display it
			handler = "javascript:var wicket=" + handler;
		}

		tag.put(event, handler);
	}

	/**
	 * 
	 * @return event handler
	 */
	protected String getEventHandler()
	{
		return "wicketAjaxGet('" + getCallbackUrl() + "');";
	}

	/**
	 * 
	 * @param event
	 */
	protected void onCheckEvent(final String event)
	{

	}

	/**
	 * 
	 * @return event
	 */
	protected final String getEvent()
	{
		return event;
	}

	/**
	 * 
	 * @see wicket.ajax.AjaxBehavior#respond(wicket.ajax.AjaxRequestTarget)
	 */
	protected final void respond(final AjaxRequestTarget target)
	{
		onEvent(target);
	}

	/**
	 * 
	 * @param target
	 */
	protected abstract void onEvent(final AjaxRequestTarget target);
}
