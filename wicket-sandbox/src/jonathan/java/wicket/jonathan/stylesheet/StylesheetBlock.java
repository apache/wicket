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
package wicket.jonathan.stylesheet;

import wicket.Component;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.WicketTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;

/**
 * A block where stylesheet information can be inserted inline.
 * 
 * @author Jonathan Locke
 */
public abstract class StylesheetBlock extends WebComponent
{
	/** Serial Version ID. */
	private static final long serialVersionUID = 209001445308790198L;

	/** Block of stylesheet information */
	private String block;

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public StylesheetBlock(String name)
	{
		super(name);

		// Visit all siblings and children which edit stylesheet blocks
		getParent().visitChildren(IStylesheetBlockEditor.class, new IVisitor()
		{
			public Object component(final Component component)
			{
				// Let the component edit the stylsheet block
				((IStylesheetBlockEditor)component).edit(StylesheetBlock.this);
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	/**
	 * Override this method if you need to rearrange or otherwise edit the links
	 * contributed by components to resolve conflicts in ordering.
	 */
	protected void edit()
	{
	}

	/**
	 * @see wicket.markup.html.WebComponent#onRender()
	 */
	protected void onRender()
	{
		// Allow subclass an opportunity to edit the stylesheet block
		edit();

		// Get markup stream
		final MarkupStream markupStream = findMarkupStream();

		// Get next tag
		final ComponentTag tag = markupStream.getTag();

		// Must be <wicket:stylesheet/>
		if (tag instanceof WicketTag && tag.isOpenClose("wicket:stylesheet"))
		{
			// Skip tag entirely, removing it from output
			markupStream.next();

			// Write out the block inside a stylesheet block
			getResponse().write("<style type='text/css'>");
			getResponse().write(block);
			getResponse().write("</style>");
		}
		else
		{
			throw new WicketRuntimeException("Must be attached to a <wicket:stylesheet/> tag");
		}
	}
}
