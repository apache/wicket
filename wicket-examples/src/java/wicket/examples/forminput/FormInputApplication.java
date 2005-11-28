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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import wicket.examples.WicketExampleApplication;
import wicket.markup.html.ServerAndClientTimeFilter;
import wicket.markup.html.image.resource.DefaultButtonImageResource;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestWithCryptedUrl;
import wicket.protocol.http.WebResponse;
import wicket.protocol.http.WebResponseWithCryptedUrl;
import wicket.util.crypt.SunJceCrypt;

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
		getPages().setHomePage(FormInput.class);
		getSettings().setThrowExceptionOnMissingResource(false);

		getSettings().setCryptClass(SunJceCrypt.class);

		addResponseFilter(new ServerAndClientTimeFilter());

		getSharedResources().add("save", Locale.SIMPLIFIED_CHINESE,
				new DefaultButtonImageResource("\u4E4B\u5916"));
		getSharedResources().add("reset", Locale.SIMPLIFIED_CHINESE,
				new DefaultButtonImageResource("\u91CD\u65B0\u8BBE\u7F6E"));
	}


	/**
	 * @see wicket.protocol.http.WebApplication#newWebRequest(javax.servlet.http.HttpServletRequest)
	 */
	protected WebRequest newWebRequest(HttpServletRequest servletRequest)
	{
		return new WebRequestWithCryptedUrl(servletRequest);
	}

	/**
	 * @see wicket.protocol.http.WebApplication#newWebResponse(javax.servlet.http.HttpServletResponse)
	 */
	protected WebResponse newWebResponse(HttpServletResponse servletResponse)
	{
		return new WebResponseWithCryptedUrl(servletResponse);
	}


}
