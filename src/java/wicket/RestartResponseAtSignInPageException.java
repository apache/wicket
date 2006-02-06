/*
 * $Id$
 * $Revision$ $Date$
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
 * with the login page
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class RestartResponseAtSignInPageException extends AbstractRestartResponseException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public RestartResponseAtSignInPageException()
	{
		Application app = Application.get();
		Class signIn = app.getApplicationSettings().getSignInPage();
		if (signIn == null)
		{
			throw new IllegalStateException(
					"RestartResponseAtSignInPageException cannot be thrown if SignInPage setting is null in application settings");
		}
		RequestCycle.get().setResponsePage(signIn);
	}

}
