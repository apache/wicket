/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.forminput;

import java.util.Locale;

import wicket.examples.WicketExampleApplication;
import wicket.markup.html.ServerAndClientTimeFilter;
import wicket.markup.html.image.resource.DefaultButtonImageResource;

/**
 * Application class for form input example.
 * 
 * @author Eelco Hillenius
 */
public class FormInputApplication extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public FormInputApplication()
	{
	}
	
	/**
	 * @see wicket.protocol.http.WebApplication#init()
	 */
	protected void init()
	{
		getExceptionSettings().setThrowExceptionOnMissingResource(false);
		getRequestCycleSettings().addResponseFilter(new ServerAndClientTimeFilter());
		getMarkupSettings().setStripWicketTags(true);
		getSharedResources().add("save", Locale.SIMPLIFIED_CHINESE,
				new DefaultButtonImageResource("\u4E4B\u5916"));
		getSharedResources().add("reset", Locale.SIMPLIFIED_CHINESE,
				new DefaultButtonImageResource("\u91CD\u65B0\u8BBE\u7F6E"));
		
		getApplicationSettings().setContextPath("http://localhost:8080/wicket");
	}

	/**
	 * @see wicket.protocol.http.WebApplication#newWebRequest(javax.servlet.http.HttpServletRequest)
	protected WebRequest newWebRequest(HttpServletRequest servletRequest)
	{
		return new WebRequestWithCryptedUrl(servletRequest);
	}
	 */

	/**
	 * @see wicket.protocol.http.WebApplication#newWebResponse(javax.servlet.http.HttpServletResponse)
	protected WebResponse newWebResponse(HttpServletResponse servletResponse)
	{
		return new WebResponseWithCryptedUrl(servletResponse);
	}
	 */

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class getHomePage()
	{
		return FormInput.class;
	}
}
