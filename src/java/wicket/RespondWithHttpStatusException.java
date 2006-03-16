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
 * Causes Wicket to interrupt current request processing and immediately return
 * the specified HTTP status code. Nothing further will be output to the
 * browser.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Gili Tzabari
 * @see HttpServletResponse
 */
public class RespondWithHttpStatusException extends AbstractRestartResponseException
{
	private static final long serialVersionUID = 1L;
	private final int status;

	/**
	 * Responds with the specified http code
	 * 
	 * @param status
	 *            The http response status code
	 */
	public RespondWithHttpStatusException(int status)
	{
		this.status = status;
		
		RequestCycle rc = RequestCycle.get();
		if (rc == null)
		{
			throw new IllegalStateException(
					"This exception can only be thrown from within request processing cycle");
		}
		
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

	/**
	 * 
	 * @return the response status code
	 */
	public final int getStatus()
	{
		return status;
	}
}
