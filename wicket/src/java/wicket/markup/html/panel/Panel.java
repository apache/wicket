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
package wicket.markup.html.panel;

import wicket.RequestCycle;
import wicket.markup.MarkupStream;
import wicket.markup.html.HtmlContainer;


/**
 * A panel holds markup and other components.
 * @author Jonathan Locke
 */
public class Panel extends HtmlContainer
{
	/** Serial Version ID */
	private static final long serialVersionUID = -5449444447932560536L;

	/**
     * Constructor.
     * @param componentName The name of this container
     */
    public Panel(final String componentName)
    {
        super(componentName);
    }

    /**
     * Renders this component.
     * @param cycle Response to write to
     */
    protected final void handleRender(final RequestCycle cycle)
    {
        // Render the tag that included this html compoment
        final MarkupStream markupStream = findMarkupStream();

        if (!markupStream.atOpenCloseTag())
        {
            markupStream.throwMarkupException("A panel must be referenced by an openclose tag.");
        }

        renderTag(cycle, markupStream);

        // Render the associated markup
        renderAssociatedMarkup(cycle, "[panel]",
                "Markup for a panel component must begin with a component named '[panel]'");
    }
}

///////////////////////////////// End of File /////////////////////////////////
