/*
 * $Id: UploadApplication.java 5394 2006-04-16 13:36:52 +0000 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-16 13:36:52 +0000 (Sun, 16
 * Apr 2006) $
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
package wicket.examples.upload;

import javax.servlet.http.HttpServletRequest;

import wicket.Page;
import wicket.examples.WicketExampleApplication;
import wicket.extensions.ajax.markup.html.form.upload.UploadWebRequest;
import wicket.protocol.http.WebRequest;

/**
 * WicketServlet class for wicket.examples.upload example.
 * 
 * @author Eelco Hillenius
 */
public class UploadApplication extends WicketExampleApplication
{
	/**
	 * Constructor.
	 */
	public UploadApplication()
	{
	}

	/**
	 * @see wicket.examples.WicketExampleApplication#init()
	 */
	@Override
	protected void init()
	{
		getResourceSettings().setThrowExceptionOnMissingResource(false);
	}


	/**
	 * @see wicket.Application#getHomePage()
	 */
	@Override
	public Class< ? extends Page> getHomePage()
	{
		return UploadPage.class;
	}

	/**
	 * @see wicket.protocol.http.WebApplication#newWebRequest(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected WebRequest newWebRequest(HttpServletRequest servletRequest)
	{
		return new UploadWebRequest(servletRequest);
	}
}
