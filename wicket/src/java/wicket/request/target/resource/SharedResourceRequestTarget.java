/*
 * $Id: SharedResourceRequestTarget.java,v 1.3 2005/12/30 20:20:17 jonathanlocke
 * Exp $ $Revision$ $Date$
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
package wicket.request.target.resource;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.RequestCycle;
import wicket.Resource;
import wicket.Response;
import wicket.SharedResources;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebResponse;
import wicket.request.RequestParameters;

/**
 * Default implementation of {@link ISharedResourceRequestTarget}. Target that
 * denotes a shared {@link wicket.Resource}.
 *
 * @author Eelco Hillenius
 */
public class SharedResourceRequestTarget implements ISharedResourceRequestTarget
{
  /** Logging object */
  private static final Log log = LogFactory.getLog(SharedResourceRequestTarget.class);

  private final RequestParameters requestParameters;


  /**
   * Construct.
   *
   * @param requestParameters the request parameters
   */
  public SharedResourceRequestTarget(RequestParameters requestParameters)
  {
    this.requestParameters = requestParameters;
    if (requestParameters==null)
      throw new IllegalArgumentException("requestParameters may not be null");
    else if (requestParameters.getResourceKey()==null)
      throw new IllegalArgumentException("requestParameters.getResourceKey() " +
        "may not be null");
  }

  /**
   * Respond by looking up the shared resource and delegating the actual
   * response to that resource.
   *
   * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
   */
  public void respond(RequestCycle requestCycle)
  {
    SharedResources sharedResources = requestCycle.getApplication().getSharedResources();
    final String resourceKey = getRequestParameters().getResourceKey();
    Resource resource = sharedResources.get(resourceKey);
    if (resource == null)
    {
      Response response = requestCycle.getResponse();
      if(response instanceof WebResponse)
      {
        ((WebResponse)response).getHttpServletResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
        log.error("shared resource " + resourceKey + " not found");
        return;
      }
      else
      {
        throw new WicketRuntimeException("shared resource " + resourceKey +
          " not found");
      }
    }

    if (requestParameters != null)
    {
      resource.setParameters(requestParameters.getParameters());
    }

    resource.onResourceRequested();
  }

  /**
   * @see wicket.IRequestTarget#detach(wicket.RequestCycle)
   */
  public void detach(RequestCycle requestCycle)
  {
  }

  /**
   * @see wicket.IRequestTarget#getLock(RequestCycle)
   */
  public Object getLock(RequestCycle requestCycle)
  {
    return null;
  }

  /**
   * @see wicket.request.target.resource.ISharedResourceRequestTarget#getRequestParameters()
   */
  public final RequestParameters getRequestParameters()
  {
    return requestParameters;
  }

	/**
	 * @see wicket.request.target.resource.ISharedResourceRequestTarget#getResourceKey()
	 */
	public final String getResourceKey()
	{
		return requestParameters.getResourceKey();
	}
	
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (obj instanceof SharedResourceRequestTarget)
    {
      SharedResourceRequestTarget that = (SharedResourceRequestTarget)obj;
      return getRequestParameters().getResourceKey().equals(that.getRequestParameters().getResourceKey());
    }
    return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    int result = "SharedResourceRequestTarget".hashCode();
    result += getRequestParameters().getResourceKey().hashCode();
    return 17 * result;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "[SharedResourceRequestTarget@" + hashCode() + ", resourceKey=" +
      getRequestParameters().getResourceKey() + "]";
  }
}
