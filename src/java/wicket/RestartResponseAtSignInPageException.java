/*
 * $Id: RestartResponseAtSignInPageException.java,v 1.1 2006/02/06 08:27:03
 * ivaynberg Exp $ $Revision$ $Date$
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
 * Causes Wicket to interrupt current request processing and immediately respond
 * with the sign-in page. When sign-in has occurred, the user will be directed
 * to the original request.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Jonathan Locke
 */
public class RestartResponseAtSignInPageException extends RestartResponseAtInterceptPageException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public RestartResponseAtSignInPageException()
	{
		super(Application.get().getApplicationSettings().getSignInPage());
	}
}
