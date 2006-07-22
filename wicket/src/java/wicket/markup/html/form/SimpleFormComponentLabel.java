/*
 * $Id: SimpleFormComponentLabel.java 5860 2006-05-25 20:29:28 +0000 (Thu, 25
 * May 2006) eelco12 $ $Revision$ $Date: 2006-05-25 20:29:28 +0000 (Thu,
 * 25 May 2006) $
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

import wicket.MarkupContainer;
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
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            component id
	 * @param fc
	 *            form component
	 */
	@SuppressWarnings("unchecked")
	public SimpleFormComponentLabel(MarkupContainer parent, String id, FormComponent fc)
	{
		super(parent, id, fc);
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
	@Override
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		replaceComponentTagBody(markupStream, openTag, getModelObjectAsString());
	}
}