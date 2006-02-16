/*
 * $Id: AjaxSelfUpdatingTimerBehavior.java,v 1.2 2006/02/02 11:40:46 jdonnerstag
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
package wicket.ajax;

import wicket.markup.ComponentTag;
import wicket.util.time.Duration;

/**
 * Automatically re-renders the component it is attached to via AJAX at a regular
 * interval.
 */
public class AjaxSelfUpdatingTimerBehavior extends AbstractAjaxTimerBehavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param updateInterval
	 *            Duration between AJAX callbacks
	 */
	public AjaxSelfUpdatingTimerBehavior(final Duration updateInterval)
	{
		super(updateInterval);
	}

	/**
	 * 
	 * @see wicket.behavior.AbstractAjaxBehavior#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		// make sure this component is rendered with an id so its markup can be
		// found and replaced
		super.onComponentTag(tag);
		if (!tag.getAttributes().containsKey("id"))
		{
			tag.put("id", getComponent().getMarkupId());
		}
	}

	/**
	 * @see wicket.ajax.AbstractAjaxTimerBehavior#onTimer(wicket.ajax.AjaxRequestTarget)
	 */
	protected final void onTimer(final AjaxRequestTarget target)
	{
		target.addComponent(getComponent());
	}

	/**
	 * Give the subclass a chance to add something to the target, like a
	 * javascript effect call. Called after the hosting component has been added
	 * to the target.
	 * 
	 * @param target
	 *            The AJAX target
	 */
	protected void onPostProcessTarget(final AjaxRequestTarget target)
	{
	}
}
