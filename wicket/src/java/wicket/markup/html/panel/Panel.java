/*
 * $Id$ $Revision:
 * 1.10 $ $Date$
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
package wicket.markup.html.panel;

import wicket.markup.MarkupStream;
import wicket.markup.html.WebContainer;

/**
 * A panel is a reusable component that holds markup and other components.
 * <p>
 * Whereas WebContainer is an inline container like
 * <pre>
 *  ...
 *  &lt;span id=&quot;wicket-xxx&quot;&gt;
 *    &lt;span id=&quot;wicket-mylabel&quot;&gt;My label&lt;/span&gt;
 *    ....
 *  &lt;/span&gt;
 *  ...
 * </pre>
 * a Panel has its own associated markup file and the container content is
 * taken from that file, like:
 * <pre>
 *  &lt;span id=&quot;wicket-mypanel&quot;/&gt;
 * 
 *  TestPanel.html
 *  &lt;wicket:panel&gt;
 *    &lt;span id=&quot;wicket-mylabel&quot;&gt;My label&lt;/span&gt;
 *    ....
 *  &lt;/wicket:panel&gt;
 * </pre>
 * 
 * @author Jonathan Locke
 */
public class Panel extends WebContainer
{
    /** Serial Version ID */
    private static final long serialVersionUID = -5449444447932560536L;

    /**
     * @see wicket.Component#Component(String)
     */
    public Panel(final String componentName)
    {
        super(componentName);
    }

    /**
     * Renders this component.
     */
    protected final void handleRender()
    {
        // Render the tag that included this html compoment
        final MarkupStream markupStream = findMarkupStream();

        if (!markupStream.atOpenCloseTag())
        {
            markupStream.throwMarkupException("A panel must be referenced by an openclose tag.");
        }

        renderComponentTag(markupStream);

        // Render the associated markup
        renderAssociatedMarkup("panel",
                "Markup for a panel component must begin with '<wicket:panel>'");
    }
}


