/*
 * $Id: RestartResponseAtInterceptPageException.java,v 1.10 2006/02/13 00:16:32
 * jonathanlocke Exp $ $Revision: 1.15 $ $Date: 2006/02/13 09:27:16 $
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

import javax.servlet.http.HttpServletResponse;

import wicket.protocol.http.WebResponse;
import wicket.request.target.basic.EmptyRequestTarget;

/**
 * Causes Wicket to abort processing and set the specified HTTP status code. An
 * {@link IllegalStateException} will be thrown if HTTP status code could not be
 * set and the optional parameter is specified as false.
 * 
 * This exception can be thrown from a page or a resource.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Gili Tzabari
 * @see HttpServletResponse
 */
public class AbortWithHttpStatusException extends AbortException
{
	private static final long serialVersionUID = 1L;
	private final int status;

	/**
	 * Constructor
	 * 
	 * @param status
	 *            The http response status code
	 * @param statusCodeOptional
	 *            If true and http status could not be set, an
	 *            IllegalStateException will be thrown
	 */
	public AbortWithHttpStatusException(int status, boolean statusCodeOptional)
	{
		this.status = status;

		RequestCycle rc = RequestCycle.get();
		if (rc == null)
		{
			if (!statusCodeOptional)
			{
				throw new IllegalStateException(
						"This exception can only be thrown from within request processing cycle");
			}
		}
		else
		{
			Response r = rc.getResponse();
			if (!(r instanceof WebResponse))
			{
				throw new IllegalStateException(
						"This exception can only be thrown when wicket is processing an http request");
			}

			WebResponse wr = (WebResponse)r;
			wr.getHttpServletResponse().setStatus(status);

			// abort any further response processing
			rc.setRequestTarget(EmptyRequestTarget.getInstance());
		}
	}

	/**
	 * 
	 * @return the response status code
	 */
	public final int getStatus()
	{
		return status;
	}
}
