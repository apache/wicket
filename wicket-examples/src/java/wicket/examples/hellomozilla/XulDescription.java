/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.hellomozilla;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.model.IModel;

/**
 * Simple XUL description component. Maps to a XUL description element.
 *
 * @author Eelco Hillenius
 */
public class XulDescription extends WebComponent
{
	/**
     * Constructor that uses the provided {@link IModel} as its model. All components have
     * names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the model
     * @throws wicket.WicketRuntimeException Thrown if the component has
     * been given a null name.
     */
    public XulDescription(String name, IModel model)
    {
        super(name, model);
    }

    /**
     * Allows modification of component tag.
     * @param tag The tag to modify
     * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
     */
    protected final void onComponentTag(final ComponentTag tag)
    {
        checkComponentTag(tag, "description");
        super.onComponentTag(tag);
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