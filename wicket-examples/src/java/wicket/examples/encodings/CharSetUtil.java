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
package wicket.examples.encodings;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.RenderException;
import wicket.RequestCycle;
import wicket.protocol.http.HttpApplication;


/**
 * Everybody's favorite example.
 * @author Jonathan Locke
 */
public class CharSetUtil 
{
	private static Log log = LogFactory.getLog(CharSetUtil.class);
	
	private static CharSetMap charSetMap;
	
    /**
     * Constructor
     */
    public CharSetUtil()
    {
    }
    
    /**
     * Because only servlet 2.4 supports web.xml locale-encoding-mapping-list
     * deployment descriptors, this is a workaround for 
     * servlet 2.3 
     * @param cycle
     */
    private synchronized static final void initialize(final RequestCycle cycle)
    {
    	if (charSetMap == null)
   		{
            // Get servlet context
            final ServletContext context = ((HttpApplication) cycle.getApplication()).getServletContext();
    		final InputStream inputStream = context.getResourceAsStream("/WEB-INF/" + CharSetMap.CHARSET_RESOURCE);
    		if (inputStream == null)
    		{
        		charSetMap = new CharSetMap();

    			log.debug("File '" + CharSetMap.CHARSET_RESOURCE + "' not found");
    		}
    		else
    		{
    			try
    			{
    	    		charSetMap = new CharSetMap(inputStream);
    			}
    			catch (IOException ex)
    			{
    				throw new RenderException("Error while reading CharSetMap", ex);
    			}
    		}
    	}
    }
    
    /**
     * Because only servlet 2.4 supports web.xml locale-encoding-mapping-list
     * deployment descriptors, this is a workaround for 
     * servlet 2.3 
     * @param cycle
     * @return Char set to use for response.
     */
    public final static String configureResponse(final RequestCycle cycle)
    {
    	if (charSetMap == null)
   		{
    		initialize(cycle);
    	}
    	
		return charSetMap.getCharSet(cycle.getSession().getLocale());
    }
}

///////////////////////////////// End of File /////////////////////////////////
