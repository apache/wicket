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

/**
 * Holds an uninterpreted markup string
 * @author Jonathan Locke
 */
final class RawMarkup extends MarkupElement
{
    // The raw markup string
    private final String string;

    /**
     * Constructor
     * @param string The raw markup
     */
    RawMarkup(final String string)
    {
        this.string = string;
    }

    /**
     * @return This raw markup string
     */
    public String toString()
    {
        return string;
    }

    /**
     * Compare with a given object
     * @param o The object to compare with
     * @return True if equal
     */
    public boolean equals(final Object o)
    {
        if (o instanceof String)
        {
            return string.equals(o);
        }

        if (o instanceof RawMarkup)
        {
            return string.equals(((RawMarkup) o).string);
        }

        return false;
    }

    /**
     * @see wicket.markup.MarkupElement#toUserDebugString()
     */
    public String toUserDebugString()
    {
        return "<Raw markup>";
    }

    /**
     * @return Hashcode for this object
     */
    public int hashCode()
    {
        return string.hashCode();
    }
}

///////////////////////////////// End of File /////////////////////////////////
