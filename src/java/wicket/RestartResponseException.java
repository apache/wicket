/*
 * $Id: RestartResponseException.java 5844 2006-05-24 20:53:56 +0000 (Wed, 24
 * May 2006) joco01 $ $Revision$ $Date: 2006-05-24 20:53:56 +0000 (Wed,
 * 24 May 2006) $
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
package wicket;

/**
 * Causes wicket to interrupt current request processing and immediately respond
 * with the specified page.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class RestartResponseException extends AbstractRestartResponseException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Redirects to the specified bookmarkable page
	 * 
	 * @param pageClass
	 *            class of bookmarkable page
	 */
	public RestartResponseException(Class<? extends Page> pageClass)
	{
		RequestCycle.get().setResponsePage(pageClass);
	}

	/**
	 * Redirects to the specified bookmarkable page with the given page
	 * parameters
	 * 
	 * @param pageClass
	 *            class of bookmarkable page
	 * @param params
	 *            bookmarkable page parameters
	 */
	public RestartResponseException(Class<? extends Page> pageClass, PageParameters params)
	{
		RequestCycle.get().setResponsePage(pageClass, params);
	}

	/**
	 * Redirects to the specified page
	 * 
	 * @param page
	 *            redirect page
	 */
	public RestartResponseException(Page page)
	{
		RequestCycle.get().setResponsePage(page);
	}
}
