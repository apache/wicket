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
package wicket.util.file;


import wicket.util.watch.IChangeable;

/**
 * Simple extension of File that adds functionality.
 * @author Jonathan Locke
 */
public class URL extends File implements IChangeable
{
    /** java.net.URL is final and can not be subclassed */
    private final java.net.URL url;
    
	/**
     * Construct.
     * @param url A standard java URL
     */
    public URL(final java.net.URL url)
    {
        super(url != null ? url.getPath() : null);

        this.url = url;
        if (url == null)
        {
            throw new IllegalArgumentException("Parameter 'url' must not be null");
        }
    }

    /**
     * Constructor
     * @param filename
     */
    public URL(final String filename)
    {
        this(Thread.currentThread().getContextClassLoader().getResource(filename));
    }
    
    /**
     * Get the URL
     * 
     * @return java.net.URL
     */
    public java.net.URL getUrl()
    {
        return url;
    }
}

///////////////////////////////// End of File /////////////////////////////////
