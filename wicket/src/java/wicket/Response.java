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
package wicket;

import wicket.markup.ComponentTag;

/**
 * Base class for different implementations of page output.
 * @author Jonathan Locke
 */
public abstract class Response
{ // TODO finalize javadoc
    /**
     * Closes the wicket.response output stream
     */
    public void close()
    {
    }

    /**
     * @param url The URL to encode
     * @return The encoded url
     */
    public String encodeURL(final String url)
    {
        // An implementation is only required to support sessions via URL
        // rewriting.
        return url;
    }

    /**
     * @param url The URL to redirect to
     */
    public void redirect(final String url)
    {
        // No implementation of redirect is required. For example, for something
        // like output testing or writing pages to files, redirects may be
        // irrelevant.
    }

    /**
     * Set the content type on the wicket.response.
     * @param mimeType The mime type
     */
    public void setContentType(final String mimeType)
    {
        // No implementation of redirect is required. For example, for something
        // like output testing or writing pages to files, content type may be
        // irrelevant.
    }

    /**
     * @return True if the redirect method has been called, making this wicket.response a
     *         redirect
     */
    public boolean isRedirect()
    {
        return false;
    }

    /**
     * Writes the given tag to output
     * @param tag The tag to write
     */
    public final void write(final ComponentTag tag)
    {
        write(tag.toString());
    }

    /**
     * Writes the given string to output
     * @param string The string to write
     */
    public abstract void write(final String string);
}

///////////////////////////////// End of File /////////////////////////////////
