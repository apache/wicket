/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.request;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import wicket.IClusterable;
import wicket.RequestListenerInterface;
import wicket.markup.html.link.ILinkListener;
import wicket.protocol.http.request.WebRequestCodingStrategy;

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
public class RequestParameters implements IClusterable
{
	private static final long serialVersionUID = 1L;

	/** the full path to a component (might be just the page). */
	private String componentPath;

	/** any name of the page map. */
	private String pageMapName;

	/** any version number; 0 for no version. */
	private int versionNumber;

	private boolean onlyProcessIfPathActive = false;

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

	/** free-to-use map of non-reserved parameters. */
	private Map parameters;

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
	 * Gets the component registration id of any behavior.
	 * 
	 * @return behaviorId the id
	 */
	public String getBehaviorId()
	{
		return behaviorId;
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
	 * Gets any id of a non-page target component.
	 * 
	 * @return any id of a non-page target component
	 */
	public String getComponentId()
	{
		return componentId;
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
	 * Gets any name of the page map.
	 * 
	 * @return any name of the page map
	 */
	public String getPageMapName()
	{
		return pageMapName;
	}

	/**
	 * Gets free-to-use map of non-reserved parameters.
	 * 
	 * @return free-to-use map of non-reserved parameters
	 */
	public Map getParameters()
	{
		return parameters;
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
	 * Gets any resource key.
	 * 
	 * @return any resource key
	 */
	public String getResourceKey()
	{
		return resourceKey;
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
	 * Tells wicket whether this request should only be processed if the page +
	 * version specified are pointing to the last page the user accessed.
	 * 
	 * @see WebRequestCodingStrategy#IGNORE_IF_NOT_ACTIVE_PARAMETER_NAME
	 * 
	 * @return the only-process-if-path-active flag
	 */
	public boolean isOnlyProcessIfPathActive()
	{
		return onlyProcessIfPathActive;
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
	 * Sets the only-process-if-path-active flag
	 * 
	 * @param onlyProcessIfPathActive
	 * 
	 * @see #isOnlyProcessIfPathActive()
	 */
	public void setOnlyProcessIfPathActive(boolean onlyProcessIfPathActive)
	{
		this.onlyProcessIfPathActive = onlyProcessIfPathActive;
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
	 * Sets free-to-use map of non-reserved parameters.
	 * 
	 * @param parameters
	 *            free-to-use map of non-reserved parameters
	 */
	public void setParameters(Map parameters)
	{
		this.parameters = parameters;
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
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer b = new StringBuffer("[RequestParameters ");
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
			b.append(" parameters={");
			for (Iterator i = getParameters().entrySet().iterator(); i.hasNext();)
			{
				Entry entry = (Entry)i.next();
				Object value = entry.getValue();
				b.append(entry.getKey()).append("=");
				if (value != null && value instanceof Object[])
				{
					Object[] values = (Object[])value;
					if (values.length > 1)
					{
						b.append("{");
						for (int j = 0; j < values.length; j++)
						{
							b.append(values[j]);
							if (j < values.length)
							{
								b.append(",");
							}
						}
						b.append("}");
					}
					else
					{
						b.append((values.length == 1) ? values[0] : "");
					}
				}
				else
				{
					b.append(value);
				}
				if (i.hasNext())
				{
					b.append(",");
				}
			}
			b.append("}");
		}
		if (getResourceKey() != null)
		{
			b.append(" resourceKey=").append(getResourceKey());
		}
		b.append(" onlyProcessIfPathActive=").append(isOnlyProcessIfPathActive());

		b.append("]");
		return b.toString();
	}
}
