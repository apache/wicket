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
package encodings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.voicetribe.wicket.PageParameters;
import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.html.HtmlPage;
import com.voicetribe.wicket.markup.html.basic.Label;
import com.voicetribe.wicket.protocol.http.HttpResponse;

/**
 * Everybody's favorite example.
 * @author Jonathan Locke
 */
public class Home extends HtmlPage
{
	private static Log log = LogFactory.getLog(Home.class);
	
    /**
     * Constructor
     * @param parameters Page parameters
     */
    public Home(final PageParameters parameters)
    {
        add(new Label("message", "Hello world! Test: הצü"));
    }
    
    /**
     * Because only servlet 2.4 supports web.xml locale-encoding-mapping-list
     * deployment descriptors, this is a workaround for 
     * servlet 2.3 
     */
    protected void handleResponseSetup(final RequestCycle cycle)
    {
    	super.configureResponse(cycle);
    	
    	final String encoding = "text/" 
    		+ getMarkupType() 
			+ "; charset=" 
			+ CharSetUtil.configureResponse(cycle);;
    	
    	((HttpResponse)cycle.getResponse()).setContentType(encoding);
    }
}

///////////////////////////////// End of File /////////////////////////////////
