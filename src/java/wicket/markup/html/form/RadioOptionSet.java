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
package wicket.markup.html.form;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.util.lang.EnumeratedType;

/**
 * Produces a set of radio input options for a collection of values. When the form is
 * submitted, the RadioOptionSet object updates the parent RadioChoice component with the
 * selected value.
 * @author Jonathan Locke
 */
public final class RadioOptionSet extends FormComponent
{ // TODO finalize javadoc
    /** Serial Version ID */
	private static final long serialVersionUID = 2552126944567296644L;

	/** line break markup. */
	public static final Style LINE_BREAK = new Style("<br>");

	/** paragraph markup. */
    public static final Style PARAGRAPH = new Style("<p>");

    /** space markup. */
    public static final Style SPACE = new Style(" ");

    // The style to render in
    private final Style style;

    /**
     * Constructor.
     * @param componentName The component name
     * @param values The option values
     */
    public RadioOptionSet(final String componentName, final Collection values)
    {
        this(componentName, values, LINE_BREAK);
    }

    /**
     * Constructor.
     * @param componentName The component name
     * @param values The option values
     * @param style The style of layout
     */
    public RadioOptionSet(final String componentName, final Collection values, final Style style)
    {
        super(componentName, new ArrayList(values));
        this.style = style;
    }

    /**
     * @see wicket.Component#handleComponentTag(RequestCycle, ComponentTag)
     */
    protected void handleComponentTag(final RequestCycle cycle, final ComponentTag tag)
    {
        checkTag(tag, "span");
        super.handleComponentTag(cycle, tag);
    }

    /**
     * @see wicket.Component#handleBody(RequestCycle, MarkupStream,
     *      ComponentTag)
     */
    protected void handleBody(final RequestCycle cycle, final MarkupStream markupStream,
            final ComponentTag openTag)
    {
        // Buffer to hold generated body
        final StringBuffer options = new StringBuffer();

        // Get the parent RadioChoice
        final RadioChoice parent = (RadioChoice) findParent(RadioChoice.class);

        // Get currently seleced value of radio choice
        final Object selected = parent.getModelObject();

        // Iterate through values
        List values = (List) getModelObject();

        for (final Iterator iterator = values.iterator(); iterator.hasNext();)
        {
            // Get next value
            final Object value = iterator.next();

            // Add choice to parent
            final int index = parent.addRadioOption(value);

            options.append("<input name=\""
                    + parent.getPath() + "\"" + " type=\"radio\""
                    + ((selected == value) ? " checked" : "") + " value=\"" + index + "\">");

            // Add label for radio button
            //TODO support custom labels in future
            final String label = String.valueOf(value.toString());

            options.append(getLocalizer().getString(
                    getName() + "." + label, this, label));

            // Append separator
            if (iterator.hasNext())
            {
                options.append(style.toString());
            }

            // For HTML readability
            options.append('\n');
        }

        // Replace body
        replaceBody(cycle, markupStream, openTag, options.toString());
    }

    /**
     * @see wicket.markup.html.form.FormComponent#updateModel(wicket.RequestCycle)
     */
    public void updateModel(final RequestCycle cycle)
    {
    }

    /**
     * Typesafe enum for layout of radio options.
     */
    public static final class Style extends EnumeratedType
    {
        /**
         * Construct.
         * @param name style name
         */
        Style(final String name)
        {
            super(name);
        }
    }
}

///////////////////////////////// End of File /////////////////////////////////
