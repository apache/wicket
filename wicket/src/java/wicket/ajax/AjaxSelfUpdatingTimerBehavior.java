/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
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

/**
 * 
 */
public class AjaxSelfUpdatingTimerBehavior extends AjaxTimerBehavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param millis
	 */
	public AjaxSelfUpdatingTimerBehavior(final long millis)
	{
		super(millis);
	}

	/**
	 * 
	 * @see wicket.behavior.AbstractAjaxBehavior#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		// make sure this component is render with an id so its markup can be
		// found and replaced
		super.onComponentTag(tag);
		if (!tag.getAttributes().containsKey("id"))
		{
			tag.put("id", getComponent().getMarkupId());
		}
	}

	/**
	 * 
	 * @see wicket.ajax.AjaxTimerBehavior#onTimer(wicket.ajax.AjaxRequestTarget)
	 */
	protected final void onTimer(final AjaxRequestTarget target)
	{
		target.addComponent(getComponent());
	}
}
