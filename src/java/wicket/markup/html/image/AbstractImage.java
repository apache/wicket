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
package wicket.markup.html.image;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.model.IModel;

/**
 * Abstract base class for image components.
 * 
 * @author Jonathan Locke
 */
public abstract class AbstractImage extends WebComponent
{
	/** Serial Version ID */
	private static final long serialVersionUID = 555385780092173403L;

	/**
	 * @see wicket.Component#Component(String)
	 */
	public AbstractImage(final String id)
	{
		super(id);
	}

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public AbstractImage(final String id, final IModel model)
	{
		super(id, model);
	}

	/**
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "img");
		super.onComponentTag(tag);
		final String url = getPage().urlFor(getResourcePath());
		tag.put("src", getResponse().encodeURL(url).replaceAll("&", "&amp;"));
	}

	/**
	 * @return The path to the image resource that this component references
	 */
	protected abstract String getResourcePath();

	/**
	 * @see wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
	}
}
