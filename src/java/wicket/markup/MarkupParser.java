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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.ApplicationSettings;
import wicket.Page;
import wicket.WicketRuntimeException;
import wicket.util.io.Streams;
import wicket.util.parse.metapattern.MetaPattern;
import wicket.util.parse.metapattern.parsers.TagNameParser;
import wicket.util.parse.metapattern.parsers.VariableAssignmentParser;
import wicket.util.resource.Resource;
import wicket.util.resource.ResourceNotFoundException;
import wicket.util.string.StringValue;
import wicket.util.value.ValueMap;


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
/* TODO Class needs some refactoring. It is already on my laptop, but not yet committed */
public final class MarkupParser implements IMarkupParser
{
    /** Regex to find &lt;?xml encoding=... ?&gt; */
    private static final Pattern encodingPattern = Pattern.compile("<\\?xml\\s+(.*\\s)?encoding\\s*=\\s*([\"\'](.*?)[\"\']|(\\S]*)).*\\?>");

    /** Logging */
    private static final Log log = LogFactory.getLog(MarkupParser.class);

    /** Allow to have nested link regions */
    private Stack autolinkStatus;
    
    /** current autolink setting */
    private boolean automaticLinking;

    /** current column number. */
    private int columnNumber = 1;

    /** Name of desired componentName tag attribute. 
     * E.g. &lt;tag id="wicket-..."&gt; or &lt;tag wicket=..&gt; */
    private String componentNameAttribute;

    /** True to compress multiple spaces/tabs or line endings to a single space or line ending. */
    private boolean compressWhitespace;

    /** Null, if JVM default. Else taken from <?xml encoding=""> */
    private String encoding;

    /** Input to parse. */
    private String input;
    
    /** Position in parse. */
    private int inputPosition;

    /** Last place we counted lines from. */
    private int lastLineCountIndex;

    /** Current line and column numbers during parse. */
    private int lineNumber = 1;

    /** The Page (not the component) used to create autolinks */
    private Page autolinkBasePage;

    /** True to strip out HTML comments. */
    private boolean stripComments;

    /** if true, &lt;wicket:param ..&gt; tags will be removed from markup */
    private boolean stripWicketParamTag;

    /** The desired wicket tag's namespace: e.g. &lt;wicket:.. &gt; */
    private String wicketTagName;
   
    /**
     * Constructor.
     */
    public MarkupParser()
    {
    }
    
    /**
     * Constructor.
     * @param componentNameAttribute The name of the componentName attribute
     * @param wicketTagName The name of the wicket namespace
     */
    public MarkupParser(final String componentNameAttribute, final String wicketTagName)
    {
        setComponentNameAttribute(componentNameAttribute);
        setWicketNamespace(wicketTagName);
    }
    
    /**
	 * @see wicket.markup.IMarkupParser#configure(wicket.ApplicationSettings)
	 */
	public void configure(ApplicationSettings settings)
	{
        setComponentNameAttribute(settings.getComponentNameAttribute());
        setWicketNamespace(settings.getWicketNamespace());
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
        return encoding;
    }
    
    /**
     * Reads and parses markup from a Resource such as a file.
     * @param resource The file
     * @return The markup
     * @throws ParseException
     * @throws IOException
     * @throws ResourceNotFoundException
     */
    public Markup read(final Resource resource, final Page autolinkBasePage) throws ParseException, IOException,
            ResourceNotFoundException
    {        
        // Set autolink base page
        this.autolinkBasePage = autolinkBasePage;

        // reset: Must come from markup
        this.encoding = null;
        
        try
        {
            final BufferedInputStream bin = new BufferedInputStream(resource.getInputStream(), 4000);
            if (!bin.markSupported())
            {
                throw new IOException("BufferedInputStream does not support mark/reset");
            }
            
            // read ahead buffer
            final int readAheadSize = 80;
            bin.mark(readAheadSize);
            
            // read-ahead the input stream, if it starts with <?xml encoding=".." ?>.
            // If yes, set this.encoding and return the character which follow it.
            // If not, return the whole line. determineEncoding will read-ahead
            // at max. the very first line of the markup
            this.encoding = determineEncoding(bin, readAheadSize);

            // Depending the encoding determine from the markup-file, read
            // the rest either with specific encoding or JVM default
            final String markup;
            if (this.encoding == null)
            {
                bin.reset();
                markup = Streams.readString(bin);
            }
            else
            {
                // Use the encoding as specified in <?xml encoding=".." ?>
                // Don't re-read <?xml ..> again
                markup = Streams.readString(bin, encoding);
            }

            return new Markup(resource, parseMarkup(markup));
        }
        finally
        {
            resource.close();
        }
    }
            
    /**
     * Parse the markup.
     * @param string The markup
     * @return The markup
     * @throws ParseException
     */
    Markup parse(final String string) throws ParseException
    {
        return new Markup(null, parseMarkup(string));
    }

    /**
     * Counts lines between indices.
     * @param string String
     * @param end End index
     */
    private void countLinesTo(final String string, final int end)
    {
        for (int i = lastLineCountIndex; i < end; i++)
        {
            if (string.charAt(i) == '\n')
            {
                columnNumber = 1;
                lineNumber++;
            }
            else
            {
                columnNumber++;
            }
        }

        lastLineCountIndex = end;
    }

    /**
     * Read-ahead the input stream (markup file). If it starts with 
     * &lt;?xml encoding=".." ?&gt;, than set this.encoding and return null. 
     * If not, return all characters read so far. determineEncoding 
     * will read-ahead at max. the very first line of the markup.
     * 
     * @param in The markup file
     * @param readAheadSize look ahead
     * @return null, if &lt;?xml ..?&gt; has been found; else all characters read ahead
     * @throws IOException
     */
    final private String determineEncoding(final InputStream in, final int readAheadSize) throws IOException
    {
        // max one line
        StringBuffer pushBack = new StringBuffer(readAheadSize);
        
        int value;
        while ((value = in.read()) != -1)
        {
            pushBack.append((char) value);
            
            // stop at end of the first tag or end of line. If it is html without
            // newlines, stop after X bytes (= characters)
            if ((value == '>') 
                    || (value == '\n') 
                    || (value == '\r') 
                    || (pushBack.length() >= (readAheadSize - 1)))
            {
                break;
            }
        }
        
        // Does the string match the <?xml .. ?> pattern
        final Matcher matcher = encodingPattern.matcher(pushBack);
        if (!matcher.find())
        {
            // No
            return null;
        }
        
        // Extract the encoding
        String encoding = matcher.group(3);
        if ((encoding == null) || (encoding.length() == 0))
        {
            encoding = matcher.group(4);
        }
        
        return encoding;
    }

    /**
     * 
     * @param tag
     * @return
     * @throws ParseException
     */
    private boolean handleAutolinks(final ComponentTag tag) throws ParseException
    {
        if (tag == null)
        {
            return false;
        }

        // Only xml tags not already identified as Wicket components will be considered
        // for autolinking. This is because it is assumed that Wicket components
        // like images, or all other kind of Wicket Links intend to handle it
        // themselves.
        if ((automaticLinking == true) && (tag.getComponentName() == null) 
                && tag.getAttributes().containsKey("href"))
        {
            final String originalHref = tag.getAttributes().getString("href");

            // Only hrefs which reference a html can be autolinks (for now)
            final int pos = originalHref.indexOf(".html");
            if (pos <= 0)
            {
                return false;
            }
            
            // Assign a dummy name. A component resolver will pick it and
            // handle the details.
            tag.componentName = "_autolink_";
            tag.enableAutolink(true);
            
            return true;
        }
        
        // For all <wicket:link ..> tags
        if (tag instanceof ComponentWicketTag)
        {
            final ComponentWicketTag wtag = (ComponentWicketTag) tag;
            if (wtag.isLinkTag())
            {
                // Beginning of the region
    	        if (tag.isOpen())
    	        {
    	            if (autolinkStatus == null)
    	            {
    	                autolinkStatus = new Stack();
    	            }
    	            
    		        // remember the current setting to be reset after the region
    		        autolinkStatus.push(new Boolean(automaticLinking));
    		        
    		        // html allows to represent true in different ways
    		        final String autolink = tag.getAttributes().getString("autolink");
		            if ((autolink == null) 
		                    || "".equals(autolink) 
		                    || "true".equalsIgnoreCase(autolink) 
		                    || "1".equals(autolink))
		            {
		                automaticLinking = true;
		            }
		            else 
		            {
		                automaticLinking = false;
    		        }
    	        } 
    	        else if (tag.isClose())
    	        {
    	            // restore the autolink setting from before the region
    	            automaticLinking = ((Boolean)autolinkStatus.pop()).booleanValue();
    	        }
    	        else
    	        {
                    throw new ParseException(
                            "<wicket:link> can not be a open-close tag", 
                            tag.getPos());
    	        }
            }
        }
        
        return false;
    }

    /**
     * Gets the next tag from the input string.
     * @return The extracted tag.
     * @throws ParseException
     */
    private ComponentTag nextTag() throws ParseException
    {
        // Index of open bracket
        int openBracketIndex = input.indexOf('<', this.inputPosition);

        // While we can find an open tag, parse the tag
        if (openBracketIndex != -1)
        {
            // Determine line number
            countLinesTo(input, openBracketIndex);

            // Get index of closing tag and advance past the tag
            final int closeBracketIndex = input.indexOf('>', openBracketIndex);

            if (closeBracketIndex == -1)
            {
                throw new ParseException("No matching close bracket at position "
                        + openBracketIndex, this.inputPosition);
            }

            // Get the tag text between open and close brackets
            String tagText = input.substring(openBracketIndex + 1, closeBracketIndex);

            // Handle comments
            if (tagText.startsWith("!--"))
            {
                // Skip ahead to -->
                this.inputPosition = input.indexOf("-->", openBracketIndex + 4) + 3;

                if (this.inputPosition == -1)
                {
                    throw new ParseException(
                            "Unclosed comment beginning at " + openBracketIndex, openBracketIndex);
                }

                return nextTag();
            }
            else
            {
                // Type of tag
                ComponentTag.Type type = ComponentTag.OPEN;

                // If the tag ends in '/', it's a "simple" tag like <foo/>
                if (tagText.endsWith("/"))
                {
                    type = ComponentTag.OPEN_CLOSE;
                    tagText = tagText.substring(0, tagText.length() - 1);
                }
                else if (tagText.startsWith("/"))
                {
                    // The tag text starts with a '/', it's a simple close tag
                    type = ComponentTag.CLOSE;
                    tagText = tagText.substring(1);
                }

                // We don't deeply parse tags like DOCTYPE that start with !
                // or XML document definitions that start with ?
                if (tagText.startsWith("!") || tagText.startsWith("?") )
                {
                    // Move to position after the tag
                    this.inputPosition = closeBracketIndex + 1;

                    // Return next tag
                    return nextTag();
                }
                else
                {
                    // Parse remaining tag text, obtaining a tag object or null
                    // if it's invalid
                    final ComponentTag tag = parseTagText(tagText);

                    if (tag != null)
                    {
                        // Populate tag fields
                        tag.type = type;
                        tag.pos = openBracketIndex;
                        tag.length = (closeBracketIndex + 1) - openBracketIndex;
                        tag.text = input.substring(openBracketIndex, closeBracketIndex + 1);
                        tag.lineNumber = lineNumber;
                        tag.columnNumber = columnNumber;

                        // Move to position after the tag
                        this.inputPosition = closeBracketIndex + 1;

                        // Return the tag we found!
                        return tag;
                    }
                    else
                    {
                        throw new ParseException(
                                "Malformed tag (line " + lineNumber + ", column "
                                + columnNumber + ")", openBracketIndex);
                    }
                }
            }
        }

        // There is no next matching tag
        return null;
    }

    /**
     * Scans the given markup string and extracts balancing tags.
     * @param markup The markup
     * @return An immutable list of immutable MarkupElement elements
     * @throws ParseException Thrown if markup is malformed or tags don't balance
     */
    private List parseMarkup(final String markup) throws ParseException
    {
        // List to return
        final List list = new ArrayList();

        // keep a reference to the markup
        setInput(markup);

        // Tag stack to find balancing tags
        final Stack stack = new Stack();

        // Position in parse
        int position = 0;

        // This is to avoid unnecessary list scans. If any <wicket:param ..>
        // tag was found, this value will be true.
        boolean hasWicketParamTag = false;
        
        // Loop through the tags
        for (ComponentTag tag; null != (tag = nextTag());)
        {
            if(log.isDebugEnabled())
            {
                log.debug("tag: " + tag.toUserDebugString() + ", stack: " + stack);
            }
        
            // Set the flag if <wicket:param ...> was found
            if ((tag instanceof ComponentWicketTag) && "param".equalsIgnoreCase(tag.getName()))
            {
                hasWicketParamTag = true;
            }
            
            // True if tag should be added to list
            boolean addTag = false;
            
            // resolve autolinks inline
            addTag = handleAutolinks(tag);

            // Check tag type
            if (tag.type == ComponentTag.OPEN)
            {
                // Push onto stack
                stack.push(tag);

                // We add open tags if they have the componentName attribute set
                addTag |= tag.componentName != null;
            }
            else if (tag.type == ComponentTag.CLOSE)
            {
                // Check that there is something on the stack
                if (stack.size() > 0)
                {
                    // Pop the top tag off the stack
                    ComponentTag top = (ComponentTag) stack.pop();

                    // If the name of the current close tag does not match the
                    // tag on the stack
                    // then we may have a mismatched close tag
                    boolean mismatch = !top.getName().equalsIgnoreCase(tag.getName());

                    if (mismatch)
                    {
                        // Pop any simple tags off the top of the stack
                        while (mismatch && !top.requiresCloseTag())
                        {
                            // Pop simple tag
                            top = (ComponentTag) stack.pop();

                            // Does new top of stack mismatch too?
                            mismatch = !top.getName().equalsIgnoreCase(tag.getName());
                        }

                        // If adjusting for simple tags did not fix the problem,
                        // it must be a real mismatch.
                        if (mismatch)
                        {
                            throw new ParseException("Tag "
                                    + top.toUserDebugString() + " has a mismatched close tag at "
                                    + tag.toUserDebugString(), tag.getPos());
                        }
                    }

                    // Tag matches, so add pointer to matching tag
                    tag.closes = top;

                    // We want to add the tag if the open Tag on the stack had a
                    // componentName attribute
                    addTag |= top.componentName != null;
                }
                else
                {
                    throw new ParseException("Tag "
                            + tag.toUserDebugString() + " does not have a matching open tag", 
                            tag.getPos());
                }
            }
            else if (tag.type == ComponentTag.OPEN_CLOSE)
            {
                // Tag closes itself
                tag.closes = tag;

                // Does the open tag have the attribute we're looking for?
                addTag |= tag.componentName != null;
            }

            // Add tag to list?
            if (addTag)
            {
                // Add text from last position to tag position
                if (tag.getPos() > position)
                {
                    String rawMarkup = markup.substring(position, tag.getPos());

                    if (stripComments)
                    {
                        rawMarkup = new String(rawMarkup.toString()).replaceAll("<!--(.|\n|\r)*?-->", "");
                    }

                    if (compressWhitespace)
                    {
                        rawMarkup = rawMarkup.replaceAll("[ \\t]+", " ");
                        rawMarkup = rawMarkup.replaceAll("( ?[\\r\\n] ?)+", "\n");
                    }

                    list.add(new RawMarkup(rawMarkup));
                }

                if ((tag instanceof ComponentWicketTag) && ((ComponentWicketTag)tag).isLinkTag())
                {
                    final String tagString = tag.toXmlString();
	                list.add(new RawMarkup(tagString));
                }
                else
                {
	                // Add immutable tag
	                tag.makeImmutable();
	                list.add(tag);
                }
                
                // Position is after tag
                position = tag.getPos() + tag.getLength();
            }
        }

        // If there's still a non-simple tag left, it's an error
        while (stack.size() > 0)
        {
            final ComponentTag top = (ComponentTag) stack.peek();

            if (!top.requiresCloseTag())
            {
                stack.pop();
            }
            else
            {
                throw new ParseException("Tag " + top + " at " + top.getPos()
                        + " did not have a close tag", top.getPos());
            }
        }

        // Add tail?
        if (position < markup.length())
        {
            list.add(new RawMarkup(markup.substring(position, markup.length())));
        }

        // remove <wicket:remove> regions
        removePreviewComponents(list);

        // Validate wicket-param tag are following component tags, assign
        // the params to the ComponentTag immediately preceding and remove
        // the <wicket:param ..> from output.
        if (hasWicketParamTag == true)
        {
            validateWicketTags(list);
        }

        // Return an umodifable list of MarkupElements
        return Collections.unmodifiableList(list);
    }

    /**
     * Parses the text between tags. For example, "a href=foo.html".
     * @param tagText The text between tags
     * @return A new Tag object or null if the tag is invalid
     * @throws ParseException
     */
    private ComponentTag parseTagText(final String tagText) throws ParseException
    {
        // Get the length of the tagtext
        final int tagTextLength = tagText.length();
        
        // If we match tagname pattern
        final TagNameParser tagnameParser = new TagNameParser(tagText);

        if (tagnameParser.matcher().lookingAt())
        {
            // Extract the tag from the pattern matcher
            final String tagName = tagnameParser.getName();
            String namespace = tagnameParser.getNamespace();
            
            int pos;
            final ComponentTag tag;
            
            if (wicketTagName.equals(namespace))
            {
                tag = new ComponentWicketTag();
                
                // make sure this compoent will go into the list
                tag.componentName = tagName;
            }
            else
            {
                tag = new ComponentTag();
            }
            
            tag.name = tagName;
            tag.namespace = namespace;
            
            pos = tagnameParser.matcher().end(0);

            // Are we at the end? Then there are no attributes, so we just
            // return the tag
            if (pos == tagTextLength)
            {
                return tag;
            }

            // Extract attributes
            final VariableAssignmentParser attributeParser = new VariableAssignmentParser(tagText);

            while (attributeParser.matcher().find(pos))
            {
                // Get key and value using attribute pattern
                String value = attributeParser.getValue();
                
                // In case like <html xmlns:wicket> will the value be null
                if (value == null)
                {
                    value = "";
                }

                // Set new position to end of attribute
                pos = attributeParser.matcher().end(0);

                // Chop off double quotes
                if (value.startsWith("\""))
                {
                    value = value.substring(1, value.length() - 1);
                }

                // Trim trailing whitespace
                value = value.trim();

                // Get key
                final String key = attributeParser.getKey();

                // If the form <tag id = "wicket-value"> is used
                boolean wicketId = key.equalsIgnoreCase("id") && value.startsWith(componentNameAttribute + "-");

                if (wicketId)
                {
                    // extract component name from value
                    value = value.substring(componentNameAttribute.length() + 1).trim();
                }
                
                // If user-defined component name attribute is used OR the
                // standard name ("wicket") is used
                if (wicketId
                        || key.equalsIgnoreCase(componentNameAttribute)
                        || key.equalsIgnoreCase(ComponentTag.DEFAULT_COMPONENT_NAME_ATTRIBUTE))
                {
                    // Set componentName value on tag
                    tag.componentName = value;
                    
                    // value must match allowed characters
                    Matcher matcher = MetaPattern.VARIABLE_NAME.matcher(value);
                    if (!matcher.matches())
                    {
                        // TODO we should throw an exception here for 1.0
                        log.warn("WILL BE ACTIVATED SOON: Invalid character in component name '" 
                                + componentNameAttribute + "-" + value + "'"
                                + " Regex: [a-z_]+ (case insensitive)");
/*                        
                        throw new ParseException("Invalid character in component name '" 
                                + componentNameAttribute + "-" + value + "'"
                                + " Regex: [a-z_]+ (case insensitive)", 
                                tag.getPos());
*/                                
                    }
                }

                // Put the attribute in the attributes hash
                tag.attributes.put(key, StringValue.valueOf(value));

                // The input has to match exactly (no left over junk after
                // attributes)
                if (pos == tagTextLength)
                {
                    return tag;
                }
            }
            
            return tag;
        }

        return null;
    }
    
    /**
     * Removes region enclosed by <wicket:remove> tags.
     * ComponentTag are not allowed within this region for obvious reasons.
     * 
     * @param list The list to process
     */
    private void removePreviewComponents(final List list)
    {
        // Remove any <wicket:remove> components
        for (int i = 0; i < list.size(); i++)
        {
            // Get next element
            final MarkupElement element = (MarkupElement) list.get(i);

            // If element is a component tag
            if (element instanceof ComponentTag)
            {
                // Check for open tag 
                final ComponentTag openTag = (ComponentTag) element;
                
                // TODO to be removed for Wicket 1.0
                boolean remove = (openTag.isOpen() && openTag.componentName.equalsIgnoreCase("[remove]"));
                if (remove == true)
                {
                    throw new WicketRuntimeException(
                            "[remove] has been replaced by <wicket:remove>. Please modify your markup accordingly");
                }
                
                if ((remove == false) && (element instanceof ComponentWicketTag))
                {
                    remove = ((ComponentWicketTag)element).isRemoveTag();
                }
                
                if (remove == true)
                {
                    if (openTag.isOpenClose())
                    {
                        throw new MarkupException("Wicket remove tag must not be an open-close tag. Position:" + openTag.getPos());
                    }
                    
                    // Remove open tag
                    list.remove(i);

                    // If a raw markup tag follows (new value at index i after
                    // deletion)
                    if ((i < list.size()) && (list.get(i) instanceof RawMarkup))
                    {
                        // remove any raw markup
                        list.remove(i);
                    }

                    // Must have close tag
                    if ((i < list.size()) && (list.get(i) instanceof ComponentTag))
                    {
                        // Get close tag
                        ComponentTag closeTag = (ComponentTag) list.get(i);

                        // Does it close the open tag?
                        if (closeTag.closes(openTag))
                        {
                            // Remove close tag
                            list.remove(i);

                            // Back up one because i++ is coming at the bottom
                            // of the loop
                            // and we still need to process list[i].
                            i--;
                            
                            continue;
                        }
                    }

                    if (i < list.size())
                    {
                        throw new MarkupException("<wicket:remove> open tag "
                                + openTag + " not closed by " + list.get(i) 
                                + ". It must not contain a nested wicket component.");
                    }
                    else
                    {
                        throw new MarkupException("<wicket:remove> open tag "
                                + openTag + " not closed."
                        		+ " It must nt contain a nested wicket component.");
                    }
                }
            }
        }
    }

    /** 
     * Name of desired componentName tag attribute. E.g. &lt;tag id="wicket-..."&gt;
     * 
     * @param name component name 
     */
    private void setComponentNameAttribute(final String name)
    {
        this.componentNameAttribute = name;
        
        if (!ComponentTag.DEFAULT_COMPONENT_NAME_ATTRIBUTE.equals(componentNameAttribute))
        {
            log.info("You are using a non-standard component name attribute: " 
                    + componentNameAttribute);
        }
    }

    /**
     * Sets the input string to parse.
     * @param input The input string
     */
    private void setInput(final String input)
    {
        this.input = input;
        this.inputPosition = 0;
    }
    
    /** 
     * Name of the desired wicket tag napespace: e.g. &lt;wicket:..&gt; 
     * 
     * @param name wicket xml namespace (xmlns:wicket) 
     */
    private void setWicketNamespace(final String name)
    {
        this.wicketTagName = name;
        
        if (!ComponentWicketTag.DEFAULT_WICKET_NAMESPACE.equals(wicketTagName))
        {
            log.info("You are using a non-standard wicket tag name: " 
                    + wicketTagName);
        }
    }

    /**
     * Validate wicket-param tag are following component tags, assign
     * the params to the ComponentTag immediately preceding and remove
     * the <wicket:param ..> from output.
     * 
     * @param markupElements
     * @throws ParseException
     */
    // TODO this is one of methods which I'd like to see being moved out of 
    //      the parser. It has nothing todo with markup parsing.
    private final void validateWicketTags(final List markupElements)
    	throws ParseException
    {
        // For each ComponentWicketTag found ...
        for (int i=0; i < markupElements.size(); i++)
        {
            final Object elem = markupElements.get(i);
            
            if (!(elem instanceof ComponentWicketTag))
            {
                continue;
            }
            
            // There might be more than one wicket parameter tag. 
            // Find the component tag (which is not a param tag) preceding
            // that element.
            MarkupElement parentTag = (MarkupElement) elem;
            int pos = i;
            while (parentTag instanceof ComponentWicketTag)
            {
                pos -= 1;
                if (pos < 0)
                {
                    throw new ParseException(
                            "Found Wicket parameter tag without related component tag.",
                            ((ComponentTag)elem).getPos());
                }
                
                parentTag = (MarkupElement) markupElements.get(pos);
                
                // param tags may be in the next line with empty RawMarkup between 
                // the ComponentTag and the param tag. 
                if (parentTag instanceof RawMarkup)
                {
                    String text = ((RawMarkup)parentTag).toString();
                    text = text.replaceAll("\n", "");
                    text = text.replaceAll("\r", "");
                    text = text.trim();
                    if (text.length() == 0)
                    {
                        pos -= 1;
                        if (pos < 0)
                        {
                            throw new ParseException(
                                    "Found Wicket parameter tag without related component tag.",
                                    ((ComponentTag)elem).getPos());
                        }
                        
                        parentTag = (MarkupElement) markupElements.get(pos);
                    }
                }
            }
            
	        if (!(parentTag instanceof ComponentTag))
	        {
	            throw new ParseException(
	                    "Wicket parameter tag must immediately follow a wicket parameter "
	                    + "or wicket component tag.", 
	                    ((ComponentTag)elem).getPos());
	        }
	        
	        // TODO: <wicket:params name = "myProperty">My completely free text that can
	        //   contain everything</wicket:params> is currently not supported
	        
	        // Add the parameters to the component tag
	        final ComponentTag tag = (ComponentTag)parentTag;
	        ValueMap params = new ValueMap(tag.attributes);
	        params.putAll(((ComponentTag)elem).getAttributes());
	        params.makeImmutable();
	        tag.attributes = params;
	        
	        // Shall the wicket tag be removed from output?
	        if (stripWicketParamTag == true)
	        {
	            // TODO "empty" RawMarkup could also be removed: 
	            //  like <wicket:param..> being the only tag in the whole line 
	            markupElements.remove(elem);
	            
	            // adjust the index to match the removal
	            i -= 1;
	        }
        }
    }
}


