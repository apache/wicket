/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.ApplicationSettings;
import wicket.markup.parser.IMarkupFilter;
import wicket.markup.parser.IXmlPullParser;
import wicket.markup.parser.XmlPullParser;
import wicket.markup.parser.filter.AutolinkHandler;
import wicket.markup.parser.filter.HtmlHandler;
import wicket.markup.parser.filter.PreviewComponentTagRemover;
import wicket.markup.parser.filter.WicketComponentTagIdentifier;
import wicket.markup.parser.filter.WicketParamTagHandler;
import wicket.util.resource.IResource;
import wicket.util.resource.ResourceNotFoundException;


/**
 * This is a Wicket MarkupParser specifically for (X)HTML. It makes use of a
 * streaming XML parser to read the markup and IMarkupFilters to remove
 * comments, identify Wicket relevant tags, apply html specific treatments
 * etc.. <p>
 * The result will be an Markup object, which is basically a list, containing
 * Wicket relevant tags and RawMarkup.
 *
 * @author Jonathan Locke
 */
public final class MarkupParser
{
    /** Logging */
    private static final Log log = LogFactory.getLog(MarkupParser.class);

    /** Name of desired componentName tag attribute.
     * E.g. &lt;tag id="wicket-..."&gt; or &lt;tag wicket=..&gt; */
    private String componentNameAttribute = ComponentTag.DEFAULT_COMPONENT_NAME_ATTRIBUTE;

    /** True to strip out HTML comments. */
    private boolean stripComments;

    /** True to compress multiple spaces/tabs or line endings to a single space or line ending. */
    private boolean compressWhitespace;

    /** if true, <wicket:param ..> tags will be removed from markup */
    private boolean stripWicketTag;

    /** if true, "wicket-" will be removed from id="wicket-xxx" */
    private boolean stripWicketFromComponentTag = false;

    /** If true, MarkupParser will automatically create a ComponentWicketTag for
     * all tags surrounding a href attribute with a relative path to a
     * html file. E.g. &lt;a href="Home.html"&gt;
     */
    private boolean automaticLinking = false;

	/** If true and if componentNameAttribute has been changed, than not only
	 * use the new componentNameAttribute to identify wicket components, but
	 * also the DEFAULT_COMPONENT_NAME_ATTRIBUTE ("wicket"). Fall back
	 * to default. Both the new componentNameAttribute and 
	 * DEFAULT_COMPONENT_NAME_ATTRIBUTE would identify wicket components.
	 */
	private boolean applyDefaultComponentName = false;

    private IXmlPullParser xmlParser = new XmlPullParser();

    /**
     * Constructor.
     * @param xmlParser The streaming xml parser to read and parse the markup
     * @param componentNameAttribute The name of the componentName attribute
     */
    public MarkupParser(final IXmlPullParser xmlParser, final String componentNameAttribute)
    {
        this.xmlParser = xmlParser;
        this.componentNameAttribute = componentNameAttribute;
    }

    /**
     * Constructor.
     * @param xmlParser The streaming xml parser to read and parse the markup
     */
    public MarkupParser(final IXmlPullParser xmlParser)
    {
        this.xmlParser = xmlParser;
    }

    /**
	 * Configure the markup parser based on Wicket application settings
	 * @param settings Wicket application settings
	 */
	public void configure(final ApplicationSettings settings)
	{
        this.componentNameAttribute = settings.getComponentNameAttribute();
        this.stripWicketTag = settings.getStripWicketTags();
        this.stripComments = settings.getStripComments();
        this.compressWhitespace = settings.getCompressWhitespace();
        this.automaticLinking = settings.getAutomaticLinking();
        this.stripWicketFromComponentTag = settings.getStripComponentNames();
        this.applyDefaultComponentName = settings.getApplyDefaultComponentName();
	}

    /**
     * Return the encoding used while reading the markup file.
     * You need to call @see #read(Resource) first to initialise
     * the value.
     *
     * @return if null, than JVM default is used.
     */
    public String getEncoding()
    {
        return xmlParser.getEncoding();
    }

    /**
     * Reads and parses markup from a file.
     * @param resource The file
     * @return The markup
     * @throws ParseException
     * @throws IOException
     * @throws ResourceNotFoundException
     */
    public Markup readAndParse(final IResource resource) throws ParseException, IOException,
            ResourceNotFoundException
    {
        xmlParser.parse(resource);
        return new Markup(resource, parseMarkup());
    }

    /**
     * Parse the markup.
     * @param string The markup
     * @return The markup
     * @throws ParseException
     * @throws IOException
     * @throws ResourceNotFoundException
     */
    Markup parse(final String string) throws ParseException, IOException,
    	ResourceNotFoundException
    {
        xmlParser.parse(string);
        return new Markup(null, parseMarkup());
    }

    /**
     * Scans the given markup string and extracts balancing tags.
     * @return An immutable list of immutable MarkupElement elements
     * @throws ParseException Thrown if markup is malformed or tags don't balance
     */
    private List parseMarkup() throws ParseException
    {
        // List to return
        final List list = new ArrayList();

        // Chain together all the different markup filters
        final WicketComponentTagIdentifier detectWicketComponents = new WicketComponentTagIdentifier(xmlParser);
        detectWicketComponents.setComponentNameAttribute(this.componentNameAttribute);
        detectWicketComponents.setStripWicketFromComponentTag(this.stripWicketFromComponentTag);
        detectWicketComponents.setApplyDefaultComponentName(this.applyDefaultComponentName);
        
        final WicketParamTagHandler wicketParamTagHandler = new WicketParamTagHandler(
                new HtmlHandler(detectWicketComponents));
        wicketParamTagHandler.setStripWicketTag(this.stripWicketTag);
        
        final PreviewComponentTagRemover previewComponentTagRemover = new PreviewComponentTagRemover(wicketParamTagHandler);
        
        final AutolinkHandler autolinkHandler = new AutolinkHandler(previewComponentTagRemover);
        autolinkHandler.setAutomaticLinking(this.automaticLinking);

        // Markup filter chain starts with auto link handler
        final IMarkupFilter markupFilterChain = autolinkHandler;

        // Loop through tags
        for (ComponentTag tag; null != (tag = (ComponentTag)markupFilterChain.nextTag());)
        {
            boolean add = (tag.getComponentName() != null);
            if (!add && tag.getXmlTag().isClose())
            {
                add = ((tag.getOpenTag() != null) && (tag.getOpenTag().getComponentName() != null));
            }

            // Add tag to list?
            if (add)
            {
                final CharSequence text =
                    	xmlParser.getInputFromPositionMarker(tag.getPos());

                // Add text from last position to tag position
                if (text.length() > 0)
                {
                    String rawMarkup = text.toString();

                    if (stripComments)
                    {
                        rawMarkup = rawMarkup.replaceAll("<!--(.|\n|\r)*?-->", "");
                    }

                    if (compressWhitespace)
                    {
                        rawMarkup = rawMarkup.replaceAll("[ \\t]+", " ");
                        rawMarkup = rawMarkup.replaceAll("( ?[\\r\\n] ?)+", "\n");
                    }

                    list.add(new RawMarkup(rawMarkup));
                }

                // Add to list unless preview component tag remover flagged as removed
                if (!PreviewComponentTagRemover.IGNORE.equals(tag.getComponentName()))
                {
	                list.add(tag);
                }

                // Position is after tag
                xmlParser.setPositionMarker();
            }
        }

        // Add tail?
        final CharSequence text = xmlParser.getInputFromPositionMarker(-1);
        if (text.length() > 0)
        {
            list.add(new RawMarkup(text));
        }

        // Make all tags immutable. Note: We can not make tag immutable 
        // just prior to adding to the list, because <wicket:param> 
        // needs to modify its preceding tag (add the attributes). And 
        // because WicketParamTagHandler and ComponentTag are not in the 
        // same package, WicketParamTagHandler is not able to modify the
        // default protected variables of ComponentTag, either.
        for (int i=0; i < list.size(); i++)
        {
            MarkupElement elem = (MarkupElement) list.get(i);
            if (elem instanceof ComponentTag)
            {
                ((ComponentTag)elem).makeImmutable();
            }
        }
        
        // Return immutable list of all MarkupElements
        return Collections.unmodifiableList(list);
    }
}

///////////////////////////////// End of File /////////////////////////////////
