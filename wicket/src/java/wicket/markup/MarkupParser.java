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
import wicket.util.resource.Resource;
import wicket.util.resource.ResourceNotFoundException;


/**
 * A fairly shallow markup parser. Parses a markup string of a given type of markup (for
 * example, html, xml, vxml or wml) into ComponentTag and RawMarkup tokens. The ComponentTag 
 * tokens must have either the componentNameAttribute attribute or the tag's name 
 * must have the wicketTagName namespace. Tags matching these conditions are 
 * returned as ComponentTag values. Text before, between and after such tags are returned as 
 * RawMarkup values. A check is done to ensure that tags returned balance correctly.
 * MarkupParser also applies some special treatment to specific ComponentTags like 
 * &lt;wicket:remove&gt; . MarkupParser will remove all text (RawMarkup)
 * between the respective open and close tag. ComponentTags are not allowed within this region.
 * The parameters specified through &lt;wicket:param ..&gt; tags are added to the
 * immediately preceding ComponentTag. And all attributes named 'href' are flagged for later
 * automatic linking of the URL. <p>
 * Note: Why are we not using SAX or DOM parsers to read the markup? The reason is, we need only
 * a few tags as described above. All the rest is treated as text only. 
 * 
 * @author Jonathan Locke
 */

/* TODO: This class needs some re-factoring as it is growing to large and inflexible.
 * I have the following things in mind:
 * a) some kind of XML PullParser to separate XML logic from Wicket specific markup logic
 * b) some kind of filter/listener interface which gets called on specific filter conditions
 *    like e.g. id starts with "wicket-" or tag namespace == "wicket" or attribute href exists.
 * c) avoid post-processing the markup elements. <wicket:region name=remove> should simply
 *    skip all markup until the end of the region
 * d) similar is true for <wicket:param> with a flag controlling the output: write yes/no
 * e) similar is true for <wicket:link> 
 * The idea is to avoid creating ComponentTags for everything or you end up writing creating
 * your own DOM structure. This is especially true with href, which are everywhere. Besides,
 * the current approach has it's weakness if the tag, which the href attribute belongs to,
 * already is a ComponentTag, e.g. <a id="wicket-myLink" href="Home.html" text="my tooltip">. 
 * Assuming you want to localize the tooltip. You have to handle the href yourself. Automatic 
 * linking will not touch it. If it would, should it modify it before or after Component.render()?
 * May be the Component should get an additional transient variable like originalHref.
 * What about attaching an AttributeModifier if href is found? Con: AttributeModifier can 
 * only work on the tag itself, not on the tags children. Thus you again would need a component
 * per href tag or analyze the html a second time.
 */
public final class MarkupParser 
{
    /** Logging */
    private static final Log log = LogFactory.getLog(MarkupParser.class);

    /** Name of desired componentName tag attribute. 
     * E.g. &lt;tag id="wicket-..."&gt; or &lt;tag wicket=..&gt; */
    private String componentNameAttribute = ComponentTag.DEFAULT_COMPONENT_NAME_ATTRIBUTE;

    /** Name of the desired wicket tag: e.g. &lt;wicket&gt; */
    private String wicketNamespace = ComponentWicketTag.DEFAULT_WICKET_NAMESPACE;

    /** True to strip out HTML comments. */
    private boolean stripComments;

    /** True to compress multiple spaces/tabs or line endings to a single space or line ending. */
    private boolean compressWhitespace;

    /** if true, <wicket:param ..> tags will be removed from markup */
    private boolean stripWicketParamTag;
    
    /** If true, MarkupParser will automatically create a ComponentWicketTag for
     * all tags surrounding a href attribute with a relative path to a
     * html file. E.g. &lt;a href="Home.html"&gt;
     */
    private boolean automaticLinking = false;

    private IXmlPullParser xmlParser = new XmlPullParser();
    
    /**
     * Constructor.
     * @param componentNameAttribute The name of the componentName attribute
     * @param wicketNamespace The name of the wicket namespace
     */
    public MarkupParser(final IXmlPullParser xmlParser, final String componentNameAttribute, final String wicketNamespace)
    {
        this.xmlParser = xmlParser;
        this.componentNameAttribute = componentNameAttribute;
        this.wicketNamespace = wicketNamespace;
    }
    
    /**
     * Constructor.
     */
    public MarkupParser(final IXmlPullParser xmlParser)
    {
        this.xmlParser = xmlParser;
    }
    
    /**
	 * @see wicket.markup.IMarkupParser#configure(wicket.ApplicationSettings)
	 */
	public void configure(ApplicationSettings settings)
	{
        this.componentNameAttribute = settings.getComponentNameAttribute();
        this.wicketNamespace = settings.getWicketNamespace();
        this.stripWicketParamTag = settings.getStripWicketParamTag();
        this.stripComments = settings.getStripComments();
        this.compressWhitespace = settings.getCompressWhitespace();
        this.automaticLinking = settings.getAutomaticLinking();
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
    // TODO rename to parse(). See below 
    public Markup read(final Resource resource) throws ParseException, IOException,
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
     */
    Markup parse(final String string) throws ParseException
    {
        xmlParser.parse(string);
        return new Markup(null, parseMarkup());
    }

    /**
     * Scans the given markup string and extracts balancing tags.
     * @param markup The markup
     * @return An immutable list of immutable MarkupElement elements
     * @throws ParseException Thrown if markup is malformed or tags don't balance
     */
    private List parseMarkup() throws ParseException
    {
        // List to return
        final List list = new ArrayList();

        final WicketComponentTagIdentifier detectWicketComponents = new WicketComponentTagIdentifier(xmlParser);
        detectWicketComponents.setComponentNameAttribute(this.componentNameAttribute);
        detectWicketComponents.setWicketNamespace(this.wicketNamespace);

        final WicketParamTagHandler wicketParamTagHandler = new WicketParamTagHandler(
                new HtmlHandler(detectWicketComponents));
        wicketParamTagHandler.setStripWicketParamTag(this.stripWicketParamTag);

        final PreviewComponentTagRemover previewComponentTagRemover = new PreviewComponentTagRemover(wicketParamTagHandler);
        
        final AutolinkHandler autolinkHandler = new AutolinkHandler(previewComponentTagRemover);
        autolinkHandler.setAutomaticLinking(this.automaticLinking);
        
        final IMarkupFilter parser = autolinkHandler;
        
        // Loop through tags
        for (ComponentTag tag; null != (tag = (ComponentTag)parser.nextTag());)
        {
            boolean add = (tag.getComponentName() != null);
            if ((add == false) && tag.getXmlTag().isClose())
            {
                add = ((tag.getOpenTag() != null) && (tag.getOpenTag().getComponentName() != null));
            }
            
            // Add tag to list?
            if (add == true)
            {
                final CharSequence text = 
                    	xmlParser.getInputFromPositionMarker(tag.getXmlTag().getPos());
                
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

                // Add immutable tag
                tag.makeImmutable();
                list.add(tag);

                // Position is after tag
                xmlParser.setPositionMarker();
            }
        }

        // Add tail?
        final CharSequence text = ((XmlPullParser)xmlParser).getInputFromPositionMarker(-1);
        if (text.length() > 0)
        {
            list.add(new RawMarkup(text));
        }
        
        // Return immutable list of all MarkupElements
        return Collections.unmodifiableList(list);
    }
}

///////////////////////////////// End of File /////////////////////////////////
