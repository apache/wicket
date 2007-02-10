/*
 * $Id$ $Revision$ $Date$
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
package wicket.extensions.ajax.markup.html;

import wicket.ajax.IAjaxIndicatorAware;
import wicket.ajax.markup.html.AjaxLink;
import wicket.model.IModel;

/**
 * A variant of the {@link AjaxLink} that displays a busy indicator while the
 * ajax request is in progress.
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class IndicatingAjaxLink extends AjaxLink implements IAjaxIndicatorAware
{
	private final WicketAjaxIndicatorAppender indicatorAppender = new WicketAjaxIndicatorAppender();

	/**
	 * Constructor
	 * @param id
	 */
	public IndicatingAjaxLink(String id)
	{
		this(id, null);
	}

	/**
	 * Constructor
	 * @param id
	 * @param model
	 */
	public IndicatingAjaxLink(String id, IModel model)
	{
		super(id, model);
		add(indicatorAppender);
	}

	/**
	 * @see wicket.ajax.IAjaxIndicatorAware#getAjaxIndicatorMarkupId()
	 */
	public String getAjaxIndicatorMarkupId()
	{
		return indicatorAppender.getMarkupId();
	}

}
