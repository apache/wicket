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
package org.apache.wicket.protocol.http;

import org.apache.wicket.*;
import org.apache.wicket.util.string.Strings;

/**
 * A session subclass for the HTTP protocol.
 * 
 * @author Jonathan Locke
 */
public class WebSession extends Session
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor. Note that {@link RequestCycle} is not available until this
	 * constructor returns.
	 *
     * @deprecated Use #WebSession(Request) instead
     * 
	 * @param application
	 *            The application
	 * @param request
	 *            The current request
	 */
	public WebSession(final Application application, Request request)
	{
		super(application, request);
	}

	/**
	 * Constructor. Note that {@link RequestCycle} is not available until this
	 * constructor returns.
     *
     * @deprecated Use #WebSession(Request)
	 * 
	 * @param application
	 *            The application
	 * @param request
	 *            The current request
	 */
	public WebSession(final WebApplication application, Request request)
	{
		super(application, request);
	}

    /**
     * Constructor. Note that {@link RequestCycle} is not available until this
     * constructor returns.
     *
     * @param request
     *            The current request
     */
    public WebSession(Request request)
    {
        super(request);
    }

    /**
	 * @see org.apache.wicket.Session#isCurrentRequestValid(org.apache.wicket.RequestCycle)
	 */
	protected boolean isCurrentRequestValid(RequestCycle lockedRequestCycle)
	{
		WebRequest lockedRequest = (WebRequest) lockedRequestCycle.getRequest();
		
		// if the request that's holding the lock is ajax, we allow this request
		if (lockedRequest.isAjax() == true)
		{
			return true;
		}
		
		RequestCycle currentRequestCycle = RequestCycle.get();
		WebRequest currentRequest = (WebRequest) currentRequestCycle.getRequest();
		
		if (currentRequest.isAjax() == false)
		{
			// if this request is not ajax, we allow it
			return true;
		}
		
		String lockedPageId = Strings.firstPathComponent(lockedRequest.getRequestParameters().getComponentPath(), Component.PATH_SEPARATOR);
		String currentPageId = Strings.firstPathComponent(currentRequestCycle.getRequest().getRequestParameters().getComponentPath(), Component.PATH_SEPARATOR); 
		
		int lockedVersion = lockedRequest.getRequestParameters().getVersionNumber();
		int currentVersion = currentRequest.getRequestParameters().getVersionNumber();
		
		if (currentPageId.equals(lockedPageId) && currentVersion == lockedVersion) 
		{
			// we don't allow tis request
			return false;
		}
		
		return true;
	}
}
