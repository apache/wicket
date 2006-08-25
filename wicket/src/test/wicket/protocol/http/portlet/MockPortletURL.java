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

import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletURL;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

/**
 * Mock implementation of PortletURL
 * 
 * @author Janne Hietam&auml;ki (jannehietamaki)
 */
public class MockPortletURL implements PortletURL
{

	Map<String, Object> parameters=new HashMap<String, Object>();
	boolean renderUrl;
	
	/**
	 * Construct.
	 * @param renderUrl
	 */
	public MockPortletURL(boolean renderUrl){
		this.renderUrl=renderUrl;
	}
	
	public void setParameter(String key, String value)
	{
		parameters.put(key,value);
	}

	public void setParameter(String key, String[] values)
	{
		parameters.put(key,values);
	}

	@SuppressWarnings("unchecked")
	public void setParameters(Map content)
	{
		parameters=content;
	}

	public void setPortletMode(PortletMode mode) throws PortletModeException
	{
	}

	public void setSecure(boolean value) throws PortletSecurityException
	{
	}

	public void setWindowState(WindowState state) throws WindowStateException
	{
	}

	public String toString(){
		return "portlet:"+parameters;
	}
	
}
