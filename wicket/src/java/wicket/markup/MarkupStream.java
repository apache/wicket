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
package wicket.markup;

import wicket.util.resource.Resource;
import wicket.util.string.Strings;


/**
 * A stream of MarkupElements, subclases of which are Tag and RawMarkup.
 * @author Jonathan Locke
 */
public final class MarkupStream
{ // TODO finalize javadoc
    // Current index in markup stream
    private int currentIndex = 0;

    // Element at currentIndex
    private MarkupElement current;

    // The markup element list
    private final Markup markup;

    /**
     * Constructor
     * @param markup List of markup elements
     */
    public MarkupStream(final Markup markup)
    {
        this.markup = markup;

        if (markup.size() > 0)
        {
            current = get(0);
        }
    }

    /**
     * @return True if this markup stream has more MarkupElement elements
     */
    public boolean hasMore()
    {
        return currentIndex < markup.size();
    }

    /**
     * @return The next markup element in the stream
     */
    public MarkupElement next()
    {
        if (++currentIndex < markup.size())
        {
            return current = get(currentIndex);
        }

        return null;
    }

    /**
     * @return The current markup element
     */
    public MarkupElement get()
    {
        return current;
    }

    /**
     * @return The current markup element as a markup tag
     */
    public ComponentTag getTag()
    {
        if (current instanceof ComponentTag)
        {
            return (ComponentTag) current;
        }

        throwMarkupException("Tag expected");

        return null;
    }

    /**
     * @return True if current markup element is a tag
     */
    public boolean atTag()
    {
        return current instanceof ComponentTag;
    }

    /**
     * @return True if current markup element is an open tag
     */
    public boolean atOpenTag()
    {
        return atTag() && getTag().isOpen();
    }

    /**
     * @return True if current markup element is a close tag
     */
    public boolean atCloseTag()
    {
        return atTag() && getTag().isClose();
    }

    /**
     * @return True if current markup element is an openclose tag
     */
    public boolean atOpenCloseTag()
    {
        return atTag() && getTag().isOpenClose();
    }

    /**
     * @param componentName Required component name attribute
     * @return True if the current markup element is an open tag with the given component
     *         name
     */
    public boolean atOpenTag(final String componentName)
    {
        return atOpenTag() && componentName.equals(getTag().getComponentName());
    }

    /**
     * @param componentName Required component name attribute
     * @return True if the current markup element is an openclose tag with the given
     *         component name
     */
    public boolean atOpenCloseTag(final String componentName)
    {
        return atOpenCloseTag() && componentName.equals(getTag().getComponentName());
    }

    /**
     * @return Current index in markup stream
     */
    public int getCurrentIndex()
    {
        return currentIndex;
    }

    /**
     * @param currentIndex New current index in the stream
     */
    public void setCurrentIndex(final int currentIndex)
    {
        this.currentIndex = currentIndex;
        current = get(currentIndex);
    }

    /**
     * Skips any raw markup at the current position
     */
    public void skipRawMarkup()
    {
        while (current instanceof RawMarkup)
        {
            next();
        }
    }

    /**
     * Skips this component and all nested components
     */
    public final void skipComponent()
    {
        // Get start tag
        final ComponentTag startTag = getTag();

        if (startTag.isOpen())
        {
            // Skip <tag>
            next();

            // Skip nested components
            skipToMatchingCloseTag(startTag);

            // Skip </tag>
            next();
        }
        else if (startTag.isOpenClose())
        {
            // Skip <tag/>
            next();
        }
        else
        {
            // We were something other than <tag> or <tag/>
            throwMarkupException("Skip component called on bad markup element " + startTag);
        }
    }

    /**
     * Throws a new markup exception
     * @param message The exception message
     * @throws MarkupException
     */
    public void throwMarkupException(final String message)
    {
        throw new MarkupException(this, message);
    }

    /**
     * @return An HTML string highlighting the current position in the markup stream
     */
    public String toHtmlDebugString()
    {
        final StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < markup.size(); i++)
        {
            if (i == currentIndex)
            {
                buffer.append("<font color = \"red\">");
            }

            final MarkupElement element = markup.get(i);

            buffer.append(Strings.escapeMarkup(element.toString(), true));

            if (i == currentIndex)
            {
                buffer.append("</font>");
            }
        }

        return buffer.toString();
    }

    /**
     * @return The resource where this markup stream came from
     */
    public Resource getResource()
    {
        return markup.getResource();
    }

    /**
     * @param index The index of a markup element
     * @return The MarkupElement element
     */
    private MarkupElement get(final int index)
    {
        return markup.get(index);
    }

    /**
     * Renders markup until a closing tag for openTag is reached.
     * @param openTag The open tag
     */
    private void skipToMatchingCloseTag(final ComponentTag openTag)
    {
        // Loop through the markup in this container
        while (hasMore())
        {
            // If the current markup tag closes the openTag
            if (get().closes(openTag))
            {
                // Done!
                return;
            }

            // Skip element
            next();
        }
    }

    /**
     * @return String representation of markup stream
     */
    public String toString()
    {
        return "[markup = "
                + String.valueOf(markup) + ", index = " + currentIndex + ", current = "
                + ((current == null) ? "null" : current.toUserDebugString()) + "]";
    }
}

///////////////////////////////// End of File /////////////////////////////////
