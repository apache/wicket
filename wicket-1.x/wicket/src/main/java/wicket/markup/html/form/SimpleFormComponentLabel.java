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
package wicket.markup.html.form;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;

/**
 * A form component label that replaces its body with the contents of
 * {@link FormComponent#getLabel()}
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class SimpleFormComponentLabel extends FormComponentLabel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param fc
	 *            form component
	 */
	public SimpleFormComponentLabel(String id, FormComponent fc)
	{
		super(id, fc);
		if (fc.getLabel() == null)
		{
			throw new IllegalStateException("Provided form component does not have a label set. "
					+ "Use FormComponent.setLabel(IModel) to set the model "
					+ "that will feed this label");
		}
		setModel(fc.getLabel());
	}

	/**
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		replaceComponentTagBody(markupStream, openTag, getModelObjectAsString());
	}
}