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
package wicket.authorization;

/**
 * Exception that is thrown when a component may not be rendered given the
 * current authorization context, and that fact should block any rendering of
 * the current page. This exception is thrown up the hierarchy so that it can be
 * handled higher up to e.g. display an error page or a second level logon page.
 * 
 * @author Eelco Hillenius
 */
public class RenderNotAllowedException extends AuthorizationException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public RenderNotAllowedException()
	{
		super();
	}

	/**
	 * Construct.
	 * 
	 * @param message
	 */
	public RenderNotAllowedException(String message)
	{
		super(message);
	}

	/**
	 * Construct.
	 * 
	 * @param message
	 * @param cause
	 */
	public RenderNotAllowedException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Construct.
	 * 
	 * @param cause
	 */
	public RenderNotAllowedException(Throwable cause)
	{
		super(cause);
	}

}
