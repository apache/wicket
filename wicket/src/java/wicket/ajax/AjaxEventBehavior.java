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
 * An ajax behavior that is attached to a certain client-side (usually
 * javascript) event, such as onClick, onChange, onKeyDown, etc.
 * <p>
 * Example:
 * 
 * <pre>
 *        DropDownChoice choice=new DropDownChoice(...);
 *        choice.add(new AjaxEventBehavior(&quot;onchange&quot;) {
 *           protected onEvent(AjaxRequestTarget target) {
 *              System.out.println(&quot;ajax here!&quot;);
 *           }
 *        }
 * </pre>
 * 
 * This behavior will be linked to the onChange javascript event of the select
 * box this DropDownChoice represents, and so anytime a new option is selected
 * we will get the System.out message
 */
public abstract class AjaxEventBehavior extends AbstractDefaultAjaxBehavior
{
	private static final long serialVersionUID = 1L;

	private String event;

	/**
	 * Construct.
	 * 
	 * @param event
	 *            event this behavior will be attached to
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
	protected void onComponentTag(final ComponentTag tag)
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
	 * @see wicket.ajax.AbstractDefaultAjaxBehavior#respond(wicket.ajax.AjaxRequestTarget)
	 */
	protected final void respond(final AjaxRequestTarget target)
	{
		onEvent(target);
	}

	/**
	 * Listener method for the ajax event
	 * 
	 * @param target
	 */
	protected abstract void onEvent(final AjaxRequestTarget target);
}
