/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.form;

import java.io.Serializable;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;

/**
 * Multi-row text editing component.
 * 
 * @author Jonathan Locke
 */
public class TextArea extends TextComponent
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -1323747673401786242L;

	/**
     * @see wicket.Component#Component(String, Serializable)
	 */
	public TextArea(final String name, final Serializable object)
	{
		super(name, object);
	}

	/**
     * @see wicket.Component#Component(String, Serializable, String)
	 */
	public TextArea(final String name, final Serializable object, final String expression)
	{
		super(name, object, expression);
	}

	/**
	 * Handle the container's body.
	 * 
	 * @param markupStream
	 *            The markup stream
	 * @param openTag
	 *            The open tag for the body
	 * @see wicket.Component#handleComponentTagBody(MarkupStream, ComponentTag)
	 */
	protected final void handleComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		final String bodyContent;
		if (getInvalidInput() == null) 
		{
            // No validation errors
			bodyContent = getModelObjectAsString();
		}
		else
		{
            // Invalid input detected
			bodyContent = getInvalidInput();
		}
		replaceComponentTagBody(markupStream, openTag, getModelObjectAsString());
	}
}