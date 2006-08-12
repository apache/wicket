/*
 * $Id$ $Revision$
 * $Date$
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

import wicket.MarkupContainer;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.ClientEvent;
import wicket.ajax.IAjaxCallDecorator;
import wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;

/**
 * A component that allows a trigger request to be triggered via html anchor tag
 * 
 * @param <T>
 *            The type
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AjaxLink<T> extends WebMarkupContainer<T> implements IAjaxLink
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 */
	public AjaxLink(MarkupContainer parent, final String id)
	{
		this(parent, id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 * @param model
	 */
	public AjaxLink(MarkupContainer parent, final String id, final IModel<T> model)
	{
		super(parent, id, model);

		add(new AjaxEventBehavior(ClientEvent.CLICK)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				onClick(target);
			}

			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator()
			{
				return new CancelEventIfNoAjaxDecorator(AjaxLink.this.getAjaxCallDecorator());
			}

		});
	}

	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		// disable any href attr in markup
		tag.put("href", "#");
	}

	/**
	 * Returns ajax call decorator that will be used to decorate the ajax call.
	 * 
	 * @return ajax call decorator
	 */
	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		return null;
	}

	/**
	 * Listener method invoked on the ajax request generated when the user
	 * clicks the link
	 * 
	 * @param target
	 */
	public abstract void onClick(final AjaxRequestTarget target);
}
