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
package wicket.markup.html.border;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.HtmlContainer;


/**
 * A border component has associated markup which is drawn and determines placement of any
 * markup and/or components nested within the border component.
 * <p>
 * The portion of the border's associated markup file which is to be used in rendering the
 * border is denoted by a tag (of any type) with a component name of "[border]". The
 * children of the border component instance are then inserted into this markup at the
 * first tag named "[body]" in the border's associated markup.
 * <p>
 * For example, if a border's associated markup looked like this:
 * <p>
 * 
 * <pre>
 * 
 * 
 * 
 *      &lt;html&gt;
 *      &lt;body&gt;
 *        &lt;span componentName = &quot;[border]&quot;&gt;
 *            First &lt;span componentName = &quot;[body]&quot;/&gt; Last
 *        &lt;/span&gt;
 *      &lt;/body&gt;
 *      &lt;/html&gt;
 * 
 * 
 *  
 * </pre>
 * 
 * <p>
 * And the border was used on a page like this:
 * <p>
 * 
 * <pre>
 * 
 * 
 * 
 *      &lt;html&gt;
 *      &lt;body&gt;
 *        &lt;span componentName = &quot;myBorder&quot;&gt;
 *            Middle
 *        &lt;/span&gt;
 *      &lt;/body&gt;
 *      &lt;/html&gt;
 * 
 * 
 *  
 * </pre>
 * 
 * <p>
 * Then the resulting HTML would look like this:
 * <p>
 * 
 * <pre>
 * 
 * 
 * 
 *      &lt;html&gt;
 *      &lt;body&gt;
 *        &lt;span componentName = &quot;[border]&quot;&gt;
 *            First Middle Last
 *        &lt;/span&gt;
 *      &lt;/body&gt;
 *      &lt;/html&gt;
 * 
 * 
 *  
 * </pre>
 * 
 * <p>
 * In other words, the body of the myBorder component is substituted into the border's
 * associated markup at the position indicated by the "[body]" component.
 * @author Jonathan Locke
 */
public abstract class Border extends HtmlContainer
{
    /** The open tag for this border component. */
    private ComponentTag openTag;

    /**
     * Constructor.
     * @param componentName Name of border component
     */
    public Border(final String componentName)
    {
        super(componentName);
    }

    /**
     * @see wicket.Component#handleBody(wicket.RequestCycle,
     *      wicket.markup.MarkupStream,
     *      wicket.markup.ComponentTag)
     */
    protected final void handleBody(final RequestCycle cycle, final MarkupStream markupStream,
            final ComponentTag openTag)
    {
        // Save open tag for callback later to render body
        this.openTag = openTag;

        // Render the associated markup
        renderAssociatedMarkup(cycle, "[border]",
                "Markup for a border component must begin a component named '[border]'");
    }

    /**
     * Returns the open tag for this border component.
     * @return The open tag.
     */
    public final ComponentTag getOpenTag()
    {
        return openTag;
    }
}

///////////////////////////////// End of File /////////////////////////////////
