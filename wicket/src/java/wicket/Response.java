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

import java.util.Locale;

import wicket.markup.ComponentTag;

/**
 * Abstract base class for different implementations of response writing.
 * A subclass must implement write(String) to write a String to the response
 * destination (whether it be a browser, a file, a test harness or some other
 * place).  A subclass may optionally implement close(), encodeURL(String),
 * redirect(String), isRedirect() or setContentType(String) as appropriate.
 * 
 * @author Jonathan Locke
 */
public abstract class Response
{
    /**
     * Closes the response output stream
     */
    public void close()
    {
    }

    /**
     * An implementation of this method is only required if a subclass wishes
     * to support sessions via URL rewriting.  This default implementation 
     * simply returns the URL String it is passed.
     * @param url The URL to encode
     * @return The encoded url
     */
    public String encodeURL(final String url)
    {
        return url;
    }

    /**
     * A subclass may override this method to implement redirection.  Subclasses
     * which have no need to do redirection may choose not to override this 
     * default implementation, which does nothing.  For example, if a subclass 
     * wishes to write output to a file or is part of a testing harness, there
     * may be no meaning to redirection.
     * @param url The URL to redirect to
     */
    public void redirect(final String url)
    {
    }

    /**
     * Set the content type on the response, if appropriate in the subclass.
     * This default implementation does nothing.
     * @param mimeType The mime type
     */
    public void setContentType(final String mimeType)
    {        
    }
    
    /**
     * @param locale Locale to use for this response
     */
    public void setLocale(final Locale locale)
    {
    }

    /**
     * Returns true if a redirection has occurred.  The default implementation
     * always returns false since redirect is not implemented by default.
     * @return True if the redirect method has been called, making this 
     * response a redirect. 
     */
    public boolean isRedirect()
    {
        return false;
    }

    /**
     * Writes the given tag to via the write(String) abstract method.
     * @param tag The tag to write
     */
    public final void write(final ComponentTag tag)
    {
        write(tag.toString());
    }

    /**
     * Writes the given string to the Response subclass output destination.
     * @param string The string to write
     */
    public abstract void write(final String string);
}


