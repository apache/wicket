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

import wicket.util.resource.Resource;
import wicket.util.resource.ResourceNotFoundException;

/**
 * A fairly shallow markup parser. Parses a markup string of a given type of markup (for
 * example, html, xml, vxml or wml) into Tag and RawMarkup tokens. The Tag tokens must
 * have the attributeName attribute that was passed to the scanner's constructor. Only
 * tags having an attribute with the same name as the attributeName with which this markup
 * scanner was constructed are returned. Text before, between and after such tags are
 * returned as String values. A check is done to ensure that tags returned balance
 * correctly.
 * @author Jonathan Locke
 */
public interface IMarkupParser
{ // TODO finalize javadoc
    /** 
     * Name of desired componentName tag attribute.
     * @param name component name 
     */
    public void setComponentNameAttribute(final String name);
    
    /** 
     * Name of the desired wicket tag: e.g. &lt;wicket&gt; 
     * @param name wicket xml namespace (xmlns:wicket) 
     */
    public void setWicketTagName(final String name);

    /**
     * &lt;wicket:param ...&gt; tags may be included with the output for 
     * markup debugging purposes.
     *   
     * @param remove If true, markup elements will not be forwarded
     */
    public abstract void setRemoveWicketTagsFromOutput(boolean remove);

    /**
     * Return the encoding used while reading the markup file.
     * @return if null, than JVM default
     */
    public abstract String getEncoding();

    /**
     * Set whether to strip components.
     * @param stripComments whether to strip components.
     */
    public abstract void setStripComments(boolean stripComments);

    /**
     * Set whether whitespace should be compressed.
     * @param compressWhitespace whether whitespace should be compressed.
     */
    public abstract void setCompressWhitespace(boolean compressWhitespace);

    /**
     * Reads and parses markup from a file.
     * @param resource The file
     * @return The markup
     * @throws ParseException
     * @throws IOException
     * @throws ResourceNotFoundException
     */
    public abstract Markup read(final Resource resource) throws ParseException,
            IOException, ResourceNotFoundException;
}