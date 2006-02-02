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
package wicket.ajax.markup.html;

import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.markup.html.link.Link;
import wicket.model.IModel;

/**
 * 
 */
public abstract class AjaxFallbackLink extends Link
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public AjaxFallbackLink(final String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param object
	 */
	public AjaxFallbackLink(final String id, final IModel object)
	{
		super(id, object);

		add(new AjaxEventBehavior("onclick")
		{
			private static final long serialVersionUID = 1L;

			protected void onEvent(AjaxRequestTarget target)
			{
				onClick(target);
			}
			
			protected String getEventHandler()
			{
				return super.getEventHandler()+"return false;";
			}
		});
	}

	/**
	 * 
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	public final void onClick()
	{
		onClick(null);
	}
	
	protected abstract void onClick(final AjaxRequestTarget target);
}
