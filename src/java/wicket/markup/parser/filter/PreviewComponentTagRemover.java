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
package wicket.markup.parser.filter;

import java.text.ParseException;

import wicket.markup.ComponentTag;
import wicket.markup.ComponentWicketTag;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupException;
import wicket.markup.parser.IMarkupFilter;

/**
 * This is a markup inline filter. It identifies preview regions useful
 * for HTML designers to design the page. But they must be removed
 * prior to sending the markup to the client. Preview regions are enclosed
 * by &lt;wicket:remove&gt; tags.
 * 
 * @author Juergen Donnerstag
 */
public class PreviewComponentTagRemover implements IMarkupFilter
{
    /** The next MarkupFilter in the processing chain */
    private final IMarkupFilter parent;
    
    /**
     * Construct.
     * @param parent The next MarkupFilter in the processing chain
     */
    public PreviewComponentTagRemover(final IMarkupFilter parent)
    {
        this.parent = parent;
    }

    /**
     * @see wicket.markup.parser.filter.IMarkupFilter
     * @return The next MarkupFilter in the processing chain
     */
    public final IMarkupFilter getParent()
    {
        return parent;
    }

    /**
     * Removes preview regions enclosed by &lt;wicket:remove&gt; tags. 
     * Note that for obvious reasons, nested components are not 
     * allowed.
     * 
     * @see wicket.util.xml.IMarkupFilter#nextTag()
     * @return The next tag to be processed. Null, if not more tags 
     * 		   are available
     */
    public final MarkupElement nextTag() throws ParseException
    {
        // Get the next tag from the next MarkupFilter in the chain
        // If null, no more tags are available
        ComponentTag openTag = (ComponentTag) parent.nextTag();
        if (openTag == null)
        {
            return openTag;
        }

        // If it is not a remove tag, than we are finished 
        if (!(openTag instanceof ComponentWicketTag) 
                || !((ComponentWicketTag)openTag).isRemoveTag())
        {
            return openTag;
        }

        // remove tag must not be open-close tags
        if (openTag.isOpenClose())
        {
            throw new MarkupException(
                    "Wicket remove tag must not be an open-close tag: " 
                    + openTag.toUserDebugString());
        }

        // Find the corresponding close tag and remove all tags in between
        ComponentTag closeTag;
        while (null != (closeTag = (ComponentTag)parent.nextTag()))
        {
            // No Wicket component tags are allowed within the preview region.
            // Wicket components will a component name assigned.
            if (closeTag.getComponentName() == null)
            {
                continue;
            }

            // The first Wicket component following the preview region open
            // tag, must be it's corresponding close tag.
            if (closeTag.closes(openTag))
            {
                // Component's named "_ignore_" will be ignored by MarkupParser
                // and not added to the Markup.
                openTag.setComponentName("_ignore_");
                return openTag;
            }
            
            throw new MarkupException(
                    "Markup remove regions must not contain Wicket component tags. "
                    + "tag: " + closeTag.toUserDebugString());
        }
        
        throw new MarkupException(
                "Did not find close tag for markup remove region. "
                + "Open tag: " + openTag.toUserDebugString());
    }
}
