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
 * Exception that is thrown when a component that may not be enabled given the
 * current authorization context is being accessed, like calling it's listener
 * method or setting it's model.
 * 
 * @author Eelco Hillenius
 */
public class EnabledNotAllowedException extends AuthorizationException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public EnabledNotAllowedException()
	{
		super();
	}

	/**
	 * Construct.
	 * 
	 * @param message
	 */
	public EnabledNotAllowedException(String message)
	{
		super(message);
	}

	/**
	 * Construct.
	 * 
	 * @param message
	 * @param cause
	 */
	public EnabledNotAllowedException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Construct.
	 * 
	 * @param cause
	 */
	public EnabledNotAllowedException(Throwable cause)
	{
		super(cause);
	}

}
