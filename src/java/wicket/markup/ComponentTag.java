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


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import wicket.Page;
import wicket.PageParameters;
import wicket.markup.html.link.ExternalPageLink;
import wicket.util.lang.Classes;
import wicket.util.lang.EnumeratedType;
import wicket.util.parse.metapattern.Group;
import wicket.util.parse.metapattern.MetaPattern;
import wicket.util.parse.metapattern.OptionalMetaPattern;
import wicket.util.string.StringValue;
import wicket.util.string.Strings;
import wicket.util.value.ValueMap;

/**
 * A subclass of MarkupElement which represents a "significant" markup tag, such as a
 * component open or close tag. Insignificant markup tags (those which are merely
 * concerned with markup formatting operations and do not denote components or component
 * nesting) are coalesced into instances of RawMarkup (also a subclass of MarkupElement).
 *
 * @author Jonathan Locke
 */
public final class ComponentTag extends MarkupElement
{
    /**
     * Standard component name attribute always available for components regardless of
     * user ApplicationSettings for componentName attribute.
     */
    public static final String wicketComponentNameAttribute = "wcn";

    /**
     * An open tag, like &lt;TAG componentName = "xyz"&gt;.
     */
    public static final Type OPEN = new Type("OPEN");

    /**
     * A close tag, like &lt;/TAG&gt;.
     */
    public static final Type CLOSE = new Type("CLOSE");

    /**
     * An open/close tag, like &lt;TAG componentName = "xyz"/&gt;.
     */
    public static final Type OPEN_CLOSE = new Type("OPEN_CLOSE");

    /** Parse "[AutomaticComponentName:parameters]-<number>". */
    private static final Group automaticCommand = new Group(MetaPattern.WORD);

    /** pattern group for automatic parameters .*/
    private static final Group automaticParameters =
        new Group(MetaPattern.ANYTHING_NON_EMPTY);

    /** pattern group for automatic parameters .*/
    private static final Group automaticIndexPrefix =
        new Group(MetaPattern.DIGITS);

    /** pattern for automatic components. */
    private static final MetaPattern automaticComponentPattern =
        new MetaPattern(new MetaPattern[] {
            MetaPattern.LEFT_SQUARE, automaticCommand,
            new OptionalMetaPattern(new MetaPattern[] {
                    MetaPattern.COLON, automaticParameters}),
            MetaPattern.RIGHT_SQUARE,
            new OptionalMetaPattern(new MetaPattern[] {
                    MetaPattern.DASH, automaticIndexPrefix}),
            MetaPattern.DASH, MetaPattern.POSITIVE_INTEGER});

    /** Map of simple tags. */
    private static final Map doesNotRequireCloseTag = new HashMap();

    static
    {
        doesNotRequireCloseTag.put("p", Boolean.TRUE);
        doesNotRequireCloseTag.put("br", Boolean.TRUE);
        doesNotRequireCloseTag.put("img", Boolean.TRUE);
        doesNotRequireCloseTag.put("input", Boolean.TRUE);
    }

    /** Attribute map. */
    ValueMap attributes = new ValueMap();

    /** True if this tag automatically creates a corresponding component. */
    boolean automaticLink = false;

    /** Automatic link destination information. */
    Class automaticLinkPageClass;

    /** page parameters for automatic links. */
    PageParameters automaticLinkPageParameters;

    /** Any component tag that this tag closes. */
    ComponentTag closes;

    /** Column number. */
    int columnNumber;

    /** Convenient copy of componentName attribute. */
    String componentName;

    /** If mutable, the immutable tag that this tag is a mutable copy of. */
    private ComponentTag copyOf = this;

    /** True if this tag is mutable, false otherwise. */
    private boolean isMutable;

    /** Length of this tag in characters. */
    int length;

    /** Line number. */
    int lineNumber;

    /** Name of tag, such as "img" or "input". */
    String name;

    /** True if the name of this tag was changed. */
    private boolean nameChanged = false;

    /** Position of this tag in the input that was parsed. */
    int pos;

    /** Full text of tag. */
    String text;

    /** The tag type (OPEN, CLOSE or OPEN_CLOSE). */
    Type type; 

    /**
     * Construct.
     */
    public ComponentTag()
    {
        super();
    }

    /**
     * Gets whether this tag closes the provided open tag.
     * @param open The open tag
     * @return True if this tag closes the given open tag
     */
    public boolean closes(final ComponentTag open)
    {
        return (closes == open) || (closes == open.copyOf);
    }

    /**
     * Get the close tag for this tag.
     * @return Close tag for this tag
     */
    public ComponentTag closeTag()
    {
        final ComponentTag tag = new ComponentTag();

        tag.name = this.name;
        tag.type = CLOSE;
        tag.text = tag.toString();
        tag.isMutable = false;

        return tag;
    }

    /**
     * Gets a hashmap of this tag's attributes.
     * @return The tag's attributes
     */
    public ValueMap getAttributes()
    {
        return attributes;
    }

    /**
     * Get the column number.
     * @return Returns the columnNumber.
     */
    public int getColumnNumber()
    {
        return columnNumber;
    }

    /**
     * Get the name of the component.
     * @return The component name attribute of this tag
     */
    public String getComponentName()
    {
        return componentName;
    }

    /**
     * Gets the length of the tag in characters.
     * @return The tag's length
     */
    public int getLength()
    {
        return length;
    }

    /**
     * Get the line number.
     * @return Returns the lineNumber.
     */
    public int getLineNumber()
    {
        return lineNumber;
    }

    /**
     * Gets the name of the tag, for example the tag <b>'s name would be 'b'.
     * @return The tag's name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get whether the name of this component tag was changed.
     * @return Returns true if the name of this component tag was changed
     */
    public boolean getNameChanged()
    {
        return nameChanged;
    }

    /**
     * Gets the location of the tag in the input string.
     * @return Tag location (index in input string)
     */
    public int getPos()
    {
        return pos;
    }

    /**
     * Get a string attribute.
     * @param key The key
     * @return The string value
     */
    public String getString(final String key)
    {
        return attributes.getString(key);
    }

    /**
     * Get the tag type.
     * @return the tag type (OPEN, CLOSE or OPEN_CLOSE).
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Gets whether this tag represents an automatic link.
     * @return Returns the automaticLink.
     */
    public boolean isAutomaticLink()
    {
        return automaticLink;
    }

    /**
     * Gets whether this is a close tag.
     * @return True if this tag is a close tag
     */
    public boolean isClose()
    {
        return type == CLOSE;
    }

    /**
     * Gets whether this is an open tag.
     * @return True if this tag is an open tag
     */
    public boolean isOpen()
    {
        return type == OPEN;
    }

    /**
     * Gets whether this tag is an open tag with the given component name.
     * @param componentName Required component name attribute
     * @return True if this tag is an open tag with the given component name
     */
    public boolean isOpen(final String componentName)
    {
        return isOpen() && componentName.equals(componentName);
    }

    /**
     * Gets whether this tag is an open/ close tag.
     * @return True if this tag is an open and a close tag
     */
    public boolean isOpenClose()
    {
        return type == OPEN_CLOSE;
    }

    /**
     * Gets whether this tag is an openclose tag with the given component name.
     * @param componentName Required component name attribute
     * @return True if this tag is an openclose tag with the given component name
     */
    public boolean isOpenClose(final String componentName)
    {
        return isOpenClose() && componentName.equals(componentName);
    }

    /**
     * Gets whether this tag does not require a closing tag.
     * @return True if this tag does not require a closing tag
     */
    public boolean requiresCloseTag()
    {
        return doesNotRequireCloseTag.get(name) == null;
    }

    /**
     * Makes this tag object immutable by making the attribute map unmodifiable. Immutable
     * tags cannot be made mutable again. They can only be copied into new mutable tag
     * objects.
     */
    public void makeImmutable()
    {
        if (isMutable)
        {
            isMutable = false;
            attributes.makeImmutable();
        }
    }

    /**
     * Gets this tag if it is already mutable, or a mutable copy of this tag if it is immutable.
     * @return This tag if it is already mutable, or a mutable copy of this tag if it is immutable.
     */
    public ComponentTag mutable()
    {
        if (isMutable)
        {
            return this;
        }
        else
        {
            final ComponentTag tag = new ComponentTag();

            tag.name = name;
            tag.pos = pos;
            tag.length = length;
            tag.text = text;
            tag.attributes = new ValueMap(attributes);
            tag.type = type;
            tag.isMutable = true;
            tag.componentName = componentName;
            tag.closes = closes;
            tag.copyOf = copyOf;

            return tag;
        }
    }

    /**
     * Puts a boolean attribute.
     * @param key The key
     * @param value The value
     */
    public void put(final String key, final boolean value)
    {
        put(key, Boolean.toString(value));
    }

    /**
     * Puts an int attribute.
     * @param key The key
     * @param value The value
     */
    public void put(final String key, final int value)
    {
        put(key, Integer.toString(value));
    }

    /**
     * Puts a string attribute.
     * @param key The key
     * @param value The value
     */
    public void put(final String key, final String value)
    {
        attributes.put(key, value);
    }

    /**
     * Puts a {@link StringValue} attribute.
     * @param key The key
     * @param value The value
     */
    public void put(final String key, final StringValue value)
    {
        attributes.put(key, value);
    }

    /**
     * Removes an attribute.
     * @param key The key to remove
     */
    public void remove(final String key)
    {
        attributes.remove(key);
    }

    /**
     * Clears component name attribute from this tag if the tag is mutable.
     * @param componentNameAttribute The attribute name to remove
     */
    public void removeComponentName(final String componentNameAttribute)
    {
        if (isMutable)
        {
            this.componentName = null;
            remove(componentNameAttribute);
            remove(wicketComponentNameAttribute);
        }
        else
        {
            throw new UnsupportedOperationException(
                    "Attempt to clear component name attribute of immutable tag");
        }
    }

    /**
     * Sets the tag name.
     * @param name New tag name
     */
    public void setName(final String name)
    {
        if (isMutable)
        {
            this.name = name;
            this.nameChanged = true;
        }
        else
        {
            throw new UnsupportedOperationException("Attempt to set name of immutable tag");
        }
    }

    /**
     * Sets type of this tag if it is not immutable.
     * @param type The new type
     */
    public void setType(final Type type)
    {
        if (isMutable)
        {
            this.type = type;
        }
        else
        {
            throw new UnsupportedOperationException("Attempt to set type of immutable tag");
        }
    }

    /**
     * Converts this object to a string representation.
     * @return String version of this object
     */
    public String toDebugString()
    {
        return "[Tag name = " + name + ", pos = " + pos + ", line = " + lineNumber
        	+ ", length = " + length + ", attributes = ["
        	+ attributes + "], type = " + type + "]";
    }

    /**
     * Converts this object to a string representation.
     * @return String version of this object
     */
    public String toString()
    {
        if (!isMutable)
        {
            return text;
        }
        else
        {
            final StringBuffer buffer = new StringBuffer();

            buffer.append('<');

            if (type == CLOSE)
            {
                buffer.append('/');
            }

            buffer.append(name);

            if (attributes.size() > 0)
            {
                buffer.append(' ');
                buffer.append(attributes);
            }

            if (type == OPEN_CLOSE)
            {
                buffer.append('/');
            }

            buffer.append('>');

            return buffer.toString();
        }
    }

    /**
     * Automatically creates a ExternalPageLink component using the tag syntax:
     * "[autolink:parameters]", where parameters can be a list of comma separated key
     * value pairs, such as "x=9,y=foo".
     * @param page The page where the link is
     * @param markupStream The markup stream to use when throwing any exceptions
     * @return The component created by the automaticLink component command
     */
    public ExternalPageLink createAutomaticLink(
            final Page page, final MarkupStream markupStream)
    {
        // If this tag does not yet know what it's destination class and page
        // parameters are
        if (automaticLinkPageClass == null)
        {
            // look them up
            resolveAutomaticLink(page, markupStream);
        }

        // Create automaticLink bookmark link
        return new ExternalPageLink(getComponentName(), automaticLinkPageClass,
                automaticLinkPageParameters);
    }

    /**
     * Resolves the given tag's automaticLinkPageClass and automaticLinkPageParameters
     * variables by parsing the tag component name and then searching for a page class at
     * the relative URL specified by the href attribute of the tag. The href URL is
     * relative to the package containing the page where this component is contained.
     * @param page The page where the link is
     * @param markupStream Markup stream to use when throwing any exceptions
     */
    private void resolveAutomaticLink(final Page page, final MarkupStream markupStream)
    {
        // Get component name attribute
        final String componentName = getComponentName();

        // Get any automaticLink component
        final Matcher matcher = automaticComponentPattern.matcher(componentName);

        if (matcher.matches())
        {
            final String command = automaticCommand.get(matcher);
            final String parameters = automaticParameters.get(matcher);

            if (command.equals("autolink"))
            {
                // Must be anchor tag
                if (!getName().equalsIgnoreCase("a"))
                {
                    markupStream.throwMarkupException(
                            "Automatic link can only be attached to an anchor tag");
                }

                // Must have href value
                final String href = getAttributes().getString("href");

                if (href == null)
                {
                    markupStream.throwMarkupException(
                            "Automatic link requires href attribute");
                }

                // Find class relative to current package
                final String path = Strings.stripEnding(href, ".html");

                try
                {
                    automaticLinkPageClass = Classes.relativeClass(
                            page.getClass().getPackage(), path);

                    if (parameters != null)
                    {
                        automaticLinkPageParameters = new PageParameters(parameters);
                    }
                    else
                    {
                        automaticLinkPageParameters = PageParameters.NULL;
                    }
                }
                catch (ClassNotFoundException e)
                {
                    markupStream.throwMarkupException("Could not find page at " + path);
                }
            }
            else
            {
                if (!command.equals("children") && !command.equals("border"))
                {
                    markupStream.throwMarkupException(
                            "Special component names include [border], [body] and [autolink], "
                            + "but not '" + command + "'");
                }
            }
        }
        else
        {
            markupStream.throwMarkupException("Invalid syntax for automaticLink component");
        }
    }

    /**
     * Converts this object to a string representation.
     * @return String version of this object
     */
    public String toUserDebugString()
    {
        return "'" + toString() + "' (line " + lineNumber + ", column " + columnNumber + ")";
    }

    /**
     * Enumerated type for different kinds of component tags.
     */
    public static final class Type extends EnumeratedType
    {
        /**
         * Construct.
         * @param name name of type
         */
        Type(final String name)
        {
            super(name);
        }
    }
}

///////////////////////////////// End of File /////////////////////////////////
