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
package wicket.protocol.http;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.RenderException;
import wicket.Response;


/**
 * Implements responses over HTTP.
 *
 * @author Jonathan Locke
 */
public class HttpResponse extends Response
{ // TODO finalize javadoc
    /** Log. */
    private static final Log log = LogFactory.getLog(HttpResponse.class);
	
    /** The underlying response object. */
    private final HttpServletResponse httpServletResponse;
    
    /** True if response is a redirect. */
    private boolean redirect;

    /**
     * Constructor for testing harness.
     */
    HttpResponse()
    {
        this.httpServletResponse = null;
    }

    /**
     * Package private constructor.
     * @param httpServletResponse The servlet response object
     * @throws IOException
     */
    HttpResponse(final HttpServletResponse httpServletResponse) throws IOException
    {
        this.httpServletResponse = httpServletResponse;
    }

    /**
     * Gets the wrapped http servlet response object.
     * @return The wrapped http servlet response object
     */
    public final HttpServletResponse getServletResponse()
    {
        return httpServletResponse;
    }

    /**
     * Adds a cookie.
     * @param cookie The cookie to add
     */
    public final void addCookie(final Cookie cookie)
    {
        httpServletResponse.addCookie(cookie);
    }

    /**
     * Set the content type on the response.
     * @param mimeType The mime type
     */
    public final void setContentType(final String mimeType)
    {
        httpServletResponse.setContentType(mimeType);
    }

    /**
     * Output stream encoding. If the deployment descriptor contains a 
     * locale-encoding-mapping-list element, and that element provides 
     * a mapping for the given locale, that mapping is used. 
     * Otherwise, the mapping from locale to character encoding is 
     * container dependent. Default is ISO-8859-1. 
     *  
     * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
     * 
     * @param locale The locale use for mapping the character encoding
     */
    public final void setLocale(final Locale locale)
    {
    	httpServletResponse.setLocale(locale);
    }

    /**
     * Output stream encoding. See CharSetMap for more details
     * NOTE: Only available with servlet API >= 2.4
     *  
     * @param encoding e.b. ISO-8859-1 or UTF-8
     */
/*    
    public final void setCharacterEncoding(final String encoding)
    {
    	httpServletResponse.setCharacterEncoding(encoding);
    }
*/    
    /**
     * Writes string to response output.
     * @param string The string to write
     */
    public void write(final String string)
    {
    	try
    	{
    		httpServletResponse.getWriter().write(string);
    	}
    	catch (IOException ex)
    	{
    		throw new RenderException("Error while writing to servlet output writer.", ex);
    	}
    }

    /**
     * Closes response output.
     */
    public void close()
    {
    	// Servlet container will do!!
        // out.close();
    }

    /**
     * Redirects to the given url.
     * @param url The URL to redirect to
     */
    public final void redirect(final String url)
    {
        if (httpServletResponse != null)
        {
            try
            {
                if (httpServletResponse.isCommitted())
                {
                	log.error("Unable to redirect. HTTP Response has already been committed.");
                }
                
                if (log.isDebugEnabled()) 
            	{
                	log.debug("Redirecting to " + url);
            	}
                
                httpServletResponse.sendRedirect(url);
                redirect = true;
            }
            catch (IOException e)
            {
                throw new RuntimeException("Redirect failed", e);
            }
        }
    }

    /**
     * Whether this response is going to redirect the user agent.
     * @return True if this response is going to redirect the user agent
     */
    public boolean isRedirect()
    {
        return redirect;
    }

    /**
     * Returns the given url encoded.
     * @param url The URL to encode
     * @return The encoded url
     */
    public final String encodeURL(final String url)
    {
        if (httpServletResponse != null)
        {
            return httpServletResponse.encodeURL(url);
        }
        
        return url;
    }
}


