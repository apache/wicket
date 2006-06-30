/*
 * $Id: RequestParameters.java 5798 2006-05-20 15:55:29 +0000 (Sat, 20 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-20 15:55:29 +0000 (Sat, 20 May
 * 2006) $
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
package wicket.request;

import java.io.Serializable;
import java.util.Map;

import wicket.RequestListenerInterface;
import wicket.markup.html.link.ILinkListener;

/**
 * <p>
 * Object that abstracts common request parameters. It consists of possible
 * optional parameters that can be translated from e.g. servlet request
 * parameters and serves of a strongly typed variant of these that is to be
 * created by the {@link wicket.request.IRequestCycleProcessor}'s
 * {@link wicket.request.IRequestCodingStrategy}.
 * </p>
 * <p>
 * Though this object can be extended and hence more parameter options can be
 * used, anything other than in this implementation must be supported by
 * specific {@link wicket.request.IRequestCycleProcessor} implementations and
 * thus are not supported by default implementations.
 * </p>
 * 
 * @author Eelco Hillenius
 */
public class RequestParameters implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** the full path to a component (might be just the page). */
	private String componentPath;

	/** any name of the page map. */
	private String pageMapName;

	/** any version number; 0 for no version. */
	private int versionNumber;

	/** any callable interface name (e.g. {@link ILinkListener}). */
	private String interfaceName;

	/**
	 * in case this request points to a dispatched call to a behavior that is
	 * coupled to a component, this is the registration id of the behavior.
	 */
	private String behaviorId;

	/** any id of a non-page target component. */
	private String componentId;

	/** any bookmarkable page class. */
	private String bookmarkablePageClass;

	/** bookmarkable form name */
	private String bookmarkableFormName;

	/** free-to-use map of non-reserved parameters. */
	private Map<String, ? extends Object> parameters;

	/** any resource key. */
	private String resourceKey;

	/** the path info. */
	private String path;

	/**
	 * Construct.
	 */
	public RequestParameters()
	{

	}

	/**
	 * Gets any bookmarkable page class.
	 * 
	 * @return any bookmarkable page class
	 */
	public String getBookmarkablePageClass()
	{
		return bookmarkablePageClass;
	}

	/**
	 * Sets any bookmarkable page class.
	 * 
	 * @param bookmarkablePageClass
	 *            any bookmarkable page class
	 */
	public void setBookmarkablePageClass(String bookmarkablePageClass)
	{
		this.bookmarkablePageClass = bookmarkablePageClass;
	}

	/**
	 * Gets any id of a non-page target component.
	 * 
	 * @return any id of a non-page target component
	 */
	public String getComponentId()
	{
		return componentId;
	}

	/**
	 * Sets any id of a non-page target component.
	 * 
	 * @param componentId
	 *            any id of a non-page target component
	 */
	public void setComponentId(String componentId)
	{
		this.componentId = componentId;
	}

	/**
	 * Gets the full path to a component (might be just the page)..
	 * 
	 * @return the full path to a component (might be just the page).
	 */
	public String getComponentPath()
	{
		return componentPath;
	}

	/**
	 * Sets the full path to a component (might be just the page)..
	 * 
	 * @param componentPath
	 *            the full path to a component (might be just the page).
	 */
	public void setComponentPath(String componentPath)
	{
		this.componentPath = componentPath;
	}

	/**
	 * @return The interface named by these request parameters
	 */
	public RequestListenerInterface getInterface()
	{
		return RequestListenerInterface.forName(getInterfaceName());
	}

	/**
	 * Gets any callable interface name (e.g. {@link ILinkListener}).
	 * 
	 * @return any callable interface name (e.g. {@link ILinkListener})
	 */
	public String getInterfaceName()
	{
		return interfaceName;
	}

	/**
	 * Sets any callable interface name (e.g. {@link ILinkListener}).
	 * 
	 * @param interfaceName
	 *            any callable interface name (e.g. {@link ILinkListener})
	 */
	public void setInterfaceName(String interfaceName)
	{
		this.interfaceName = interfaceName;
	}

	/**
	 * Gets any name of the page map.
	 * 
	 * @return any name of the page map
	 */
	public String getPageMapName()
	{
		return pageMapName;
	}

	/**
	 * Sets any name of the page map.
	 * 
	 * @param pageMapName
	 *            any name of the page map
	 */
	public void setPageMapName(String pageMapName)
	{
		this.pageMapName = pageMapName;
	}

	/**
	 * Gets free-to-use map of non-reserved parameters.
	 * 
	 * @return free-to-use map of non-reserved parameters
	 */
	public Map<String, ? extends Object> getParameters()
	{
		return parameters;
	}

	/**
	 * Sets free-to-use map of non-reserved parameters.
	 * 
	 * @param parameters
	 *            free-to-use map of non-reserved parameters
	 */
	public void setParameters(Map<String, ? extends Object> parameters)
	{
		this.parameters = parameters;
	}

	/**
	 * Gets any resource key.
	 * 
	 * @return any resource key
	 */
	public String getResourceKey()
	{
		return resourceKey;
	}

	/**
	 * Sets any resource key.
	 * 
	 * @param resourceKey
	 *            any resource key
	 */
	public void setResourceKey(String resourceKey)
	{
		this.resourceKey = resourceKey;
	}

	/**
	 * Gets any version information string.
	 * 
	 * @return any version information string
	 */
	public int getVersionNumber()
	{
		return versionNumber;
	}

	/**
	 * Sets any version information string.
	 * 
	 * @param versionNumber
	 *            any version information string
	 */
	public void setVersionNumber(int versionNumber)
	{
		this.versionNumber = versionNumber;
	}

	/**
	 * Gets path info.
	 * 
	 * @return path info
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * Sets path info.
	 * 
	 * @param pathInfo
	 *            path info
	 */
	public void setPath(String pathInfo)
	{
		this.path = pathInfo;
	}

	/**
	 * Gets the component registration id of any behavior.
	 * 
	 * @return behaviorId the id
	 */
	public String getBehaviorId()
	{
		return behaviorId;
	}

	/**
	 * Sets the component registration id of any behavior.
	 * 
	 * @param behaviorId
	 *            the id
	 */
	public void setBehaviorId(String behaviorId)
	{
		this.behaviorId = behaviorId;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder("[RequestParameters ");
		if (getComponentPath() != null)
		{
			b.append(" componentPath=").append(getComponentPath());
			b.append(" pageMapName=").append(getPageMapName());
			b.append(" versionNumber=").append(getVersionNumber());
			b.append(" interfaceName=").append(getInterfaceName());
			b.append(" componentId=").append(getComponentId());
			b.append(" behaviorId=").append(getBehaviorId());
		}
		if (getBookmarkablePageClass() != null)
		{
			b.append(" bookmarkablePageClass=").append(getBookmarkablePageClass());
		}
		if (getParameters() != null)
		{
			b.append(" parameters=").append(getParameters());
		}
		if (getResourceKey() != null)
		{
			b.append(" resourceKey=").append(getResourceKey());
		}
		b.append("]");
		return b.toString();
	}

	/**
	 * Gets the bookmarkable form name if this was a request from a bookmarkable
	 * form.
	 * 
	 * @return String the bookmarkable form name
	 */
	public String getBookmarkableFormName()
	{
		return bookmarkableFormName;
	}

	/**
	 * @param bookmarkableFormName
	 */
	public void setBookmarkableFormName(String bookmarkableFormName)
	{
		this.bookmarkableFormName = bookmarkableFormName;
	}
}
