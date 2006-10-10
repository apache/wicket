/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.protocol.http.portlet;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletResponse;

import wicket.protocol.http.MockHttpServletResponse;

/**
 * 
 * Base class for MockPortletRequestResponse and MockPortletRenderResponse.
 * 
 * @author Janne Hietam&auml;ki (jannehietamaki)
 */
public abstract class MockPortletResponse implements PortletResponse
{
	MockHttpServletResponse resp;

	Map<String, String> properties=new HashMap<String, String>();

	/**
	 * Construct.
	 * @param resp
	 */
	public MockPortletResponse(MockHttpServletResponse resp){
		this.resp=resp;
	}

	public void addProperty(String key, String value)
	{
		properties.put(key,value);
	}

	public String encodeURL(String url)
	{
		return resp.encodeURL(url);
	}

	public void setProperty(String key, String value)
	{
		properties.put(key,value);
	}

	/**
	 * 
	 */
	public void initialize()
	{
		resp.initialize();
	}
	

    /**
     * Get the text document that was written as part of this response.
     * 
     * @return The document
     */
    public String getDocument()
    {
    	return resp.getDocument();
    }	
	
}
