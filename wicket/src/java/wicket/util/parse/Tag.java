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
package wicket.util.parse;

import wicket.util.value.ValueMap;

/**
 * Holds information for a parsed tag, such as an HTML tag.
 * @author Jonathan Locke
 */
public final class Tag
{ // TODO finalize javadoc
    int pos;

    int length;

    String name;

    String text;

    ValueMap attributes = new ValueMap();

    private boolean isMutable;

    boolean isClose;

    boolean isOpen;

    /**
     * Returns a mutable copy of this tag
     * @param tag The tag to copy
     * @return The mutable copy
     */
    public static Tag mutableCopy(final Tag tag)
    {
        final Tag newTag = new Tag();

        newTag.name = tag.name;
        newTag.pos = tag.pos;
        newTag.length = tag.length;
        newTag.attributes = new ValueMap(tag.attributes);
        newTag.isOpen = tag.isOpen;
        newTag.isClose = tag.isClose;
        newTag.isMutable = false;

        return newTag;
    }

    /**
     * Gets the name of the tag, for example the tag <b>'s name would be 'b'
     * @return The tag's name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Makes this tag object immutable by making the attribute map unmodifiable
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
     * Gets a hashmap of this tag's attributes.
     * @return The tag's attributes
     */
    public ValueMap getAttributes()
    {
        return attributes;
    }

    /**
     * The location of the tag in the input string
     * @return Tag location (index in input string)
     */
    public int getPos()
    {
        return pos;
    }

    /**
     * Gets the length of the tag in characters
     * @return The tag's length
     */
    public int getLength()
    {
        return length;
    }

    /**
     * Returns true if the tag is a close tag
     * @return True if the tag is a close tag
     */
    public boolean isClose()
    {
        return isClose;
    }

    /**
     * Returns true if the tag is a open tag
     * @return True if the tag is a open tag
     */
    public boolean isOpen()
    {
        return isOpen;
    }

    /**
     * Converts this object to a string representation
     * @return String version of this object
     */
    public String toDebugString()
    {
        return "[Tag name="
                + name + ", pos=" + pos + ", length=" + length + ", attributes=[" + attributes
                + "], isClose=" + isClose + ", isOpen=" + isOpen + "]";
    }

    /**
     * Converts this object to a string representation
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

            if (isClose && !isOpen)
            {
                buffer.append('/');
            }

            buffer.append(name);

            if (attributes.size() > 0)
            {
                buffer.append(' ');
                buffer.append(attributes);
            }

            if (isClose && isOpen)
            {
                buffer.append('/');
            }

            buffer.append('>');

            return buffer.toString();
        }
    }
}

///////////////////////////////// End of File /////////////////////////////////
