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
import wicket.markup.parser.IXmlPullParser;

/**
 * This is a markup inline filter. It identifies Wicket parameter tags
 * like &lt;wicket:param key=value/&gt; and assigns the key/value pair
 * to the attribute list of the immediately preceding Wicket component.<p>
 * Example:
 * <pre>
 * &lt;table&gt;&lt;tr id="wicket-myTable"&gt;
 *   &lt;wicket:param rowsPerPage=10/&gt;
 *   ...
 * &lt;/tr&gt;&lt;/table&gt;
 * </pre>
 * 
 * @author Juergen Donnerstag
 */
public class WicketParamTagHandler implements IMarkupFilter
{
    /** The next MarkupFilter in the chain */
    private final IMarkupFilter parent;

    /** The last Wicket component tag found in markup */
    private ComponentTag componentTag;

    /** The tag immediately preceeding the current tag */
    private ComponentTag lastTag;

    /** The last element in the chain of MarkupFilters must be a IXmlPullParser */
    private IXmlPullParser xmlParser;
    
    /** True, if wicket param tags shall be removed from output */
    private boolean stripWicketTag = true;
    
    /**
     * Construct.
     * @param parent The next MarkupFilter in the chain 
     */
    public WicketParamTagHandler(final IMarkupFilter parent)
    {
        this.parent = parent;
        
        // Find the XML parser at the end of the chain
        IMarkupFilter parser = parent.getParent();
        while (parser.getParent() != null)
        {
            parser = parser.getParent();
        }
        this.xmlParser = (IXmlPullParser)parser;
    }

    /**
     * @return The next MarkupFilter in the chain 
     */
    public final IMarkupFilter getParent()
    {
        return parent;
    }
    
    /**
     * Enable/disable removing Wicket param tags
     * @param strip True, if Wicket param tags shall be removed
     */
    public void setStripWicketTag(final boolean strip)
    {
        this.stripWicketTag = strip;
    }
    
    /**
     * Get the next tags from the next MarkupFilter in the chain. Identify
     * wicket param tags and handle them as described above.<p>
     * Note: IXmlPullParser which is the last element in the chain returns
     * XmlTag objects which are derived from MarkupElement. WicketParamTagHandler
     * hwoever assumes that the next MarkupFilter in the chain returns either
     * ComponentTags or ComponentWicketTags. Both are subclasses of MarkupElement
     * as well. Thus, WicketParamTagHandler can not be the first MarkupFilter
     * immediately following the IXmlPullParser. Some inline filter converting
     * XmlTags into ComponentTags must preceed it.
     * 
     * @see wicket.markup.parser.IMarkupFilter#nextTag()
     * @return The next MarkupElement from markup. If null, no more tags are
     *         available
     */
    public final MarkupElement nextTag() throws ParseException
    {
        // The next tag from markup. If null, no more tags are available.
        // NOTE: WE ARE EXPECTING ComponentTags. See the comment above.
        final ComponentTag tag = (ComponentTag) parent.nextTag();
        if (tag == null)
        {
            return tag;
        }

        // Handle the current tag and remember it
        lastTag = handleNext(tag);
        return lastTag;
    }
    
    /**
     * Handle wicket param tags.
     * 
     * @param tag The current tag
     * @return The next MarkupElement to be processed
     * @throws ParseException
     */
    private ComponentTag handleNext(ComponentTag tag) throws ParseException
    {
        // Ignore all close tags. Wicket params tags must not be close tags
        // and components preceding the param tag can not be close tags
        // either.
        if (tag.isClose())
        {
            return tag;
        }

        // Wicket component tags will have a component name assigned.
        // Ignore all none wicket components.
        if (tag.getComponentName() == null)
        {
            // Reset the last tag seen. Null meaning: the last tag was
            // no wicket tag.
            componentTag = null;
            return tag;
        }
        
        // By now we know it is a wicket component tag. If it is no
        // wicket param tag, than remember it and we are done.
        if (!(tag instanceof ComponentWicketTag) 
                || !((ComponentWicketTag)tag).isParamTag())
        {
            componentTag = tag;
            return tag;
        }

        // By now we know it is a Wicket param tag.
        // Only empty TEXT is allowed in between the component tag the 
        // param tag.
        final CharSequence rawMarkup = 
            	xmlParser.getInputSubsequence(
            	        lastTag.getPos() + lastTag.getLength(), 
            	        tag.getPos());
        
        if (rawMarkup.length() > 0)
        {
            String text = rawMarkup.toString();
            text = text.replaceAll("[\\r\\n]+", "").trim();

            if (text.length() > 0)
            {
                throw new MarkupException(
                        "There must not be any text between a component tag and "
                        + "it's related param tag. Only spaces and line breaks are allowed.");
            }
        }
        
        // TODO: <wicket:params name = "myProperty">My completely free text that can
        //   contain everything</wicket:params> is currently not supported
        
        // Add the parameters to the component tag
        tag.putAll(tag.getAttributes());

        // If wicket param tags shall not be included in the output, than
        // go ahead and process the next one.
        if (stripWicketTag == true)
        {
            tag = (ComponentTag)parent.nextTag();
        }
        
        return tag;
    }
}
