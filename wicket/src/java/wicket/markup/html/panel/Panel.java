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
 * A panel holds markup and other components.<p>
 * <p>
 * Whereas HTMLContainer is an inline container like
 * <pre>
 * ...
 * &lt;span id="wicket-xxx"&gt;
 *   &lt;span id="wicket-mylabel"&gt;My label&lt;/span&gt;
 *   ....
 * &lt;/span&gt;
 * ...
 * </pre>
 * A Panel does have its own associated markup file and the container
 * content is taken from that file, like:
 * <pre>
 * &lt;span id="wicket-mypanel"/&gt;
 *
 * TestPanel.html
 * &lt;span id="wicket-[panel]"&gt;
 *   &lt;span id="wicket-mylabel"&gt;My label&lt;/span&gt;
 *   ....
 * &lt;/span&gt;
 * </pre>
 * 
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
            markupStream.throwMarkupException(
                    "A panel must be referenced by an openclose tag.");
        }

        renderTag(cycle, markupStream);

        // Render the associated markup
        renderAssociatedMarkup(cycle, "panel",
                "Markup for a panel component must begin with a component '<wicket:region name=panel>'");
    }
}

///////////////////////////////// End of File /////////////////////////////////
