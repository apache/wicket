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
 * Base class for different kinds of markup: RawMarkup and Tag.
 *
 * @author Jonathan Locke
 */
public abstract class MarkupElement
{
    /**
     * Construct.
     */
    public MarkupElement()
    {
        
    }

    /**
     * Gets whether this element closes the given element.
     * @param open The open tag
     * @return True if this markup element closes the given open tag
     */
    public boolean closes(final ComponentTag open)
    {
        return false;
    }

    /**
     * Gets a string represenetation.
     * @return A string representation suitable for displaying to the user when something
     *         goes wrong.
     */
    public abstract String toUserDebugString();
}

///////////////////////////////// End of File /////////////////////////////////
