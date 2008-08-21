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
package org.apache.wicket.ajaxng.request;

import org.apache.wicket.Component;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;

/**
 * @author Matej Knopp
 */
public class AjaxNGUrlCodingStrategy implements IRequestTargetUrlCodingStrategy
{
	private final String mountPath;
	
	public AjaxNGUrlCodingStrategy(String mountPath)
	{
		this.mountPath = mountPath;
	}
	
	private Component getComponent(RequestParameters parameters)
	{
		return null;
		//String page = parameters.getParameters().get(PARAM_PAGE_ID);
	}

	public IRequestTarget decode(RequestParameters requestParameters)
	{
		return AjaxNGRequestTarget.DUMMY; 		
	}		

	public CharSequence encode(IRequestTarget requestTarget)
	{
		// we need this as the prefix for the ajax configuration 
		return getMountPath();
	}

	public String getMountPath()
	{
		return mountPath;
	}
	
	private static final String PARAM_PREFIX = "wicket:";
	public static final String PARAM_TIMESTAMP = PARAM_PREFIX + "timestamp";
	public static final String PARAM_COMPONENT_ID = PARAM_PREFIX + "componentId";
	public static final String PARAM_PAGE_ID = PARAM_PREFIX + "pageId";
	public static final String PARAM_FORM_ID = PARAM_PREFIX + "formId";
	public static final String PARAM_LISTENER_INTEFACE = PARAM_PREFIX + "listenerInterface";
	public static final String PARAM_BEHAVIOR_INDEX = PARAM_PREFIX + "behaviorIndex";

	public boolean matches(IRequestTarget requestTarget)
	{
		return requestTarget instanceof AjaxNGRequestTarget;
	}

	public boolean matches(String path)
	{
		return path.startsWith(getMountPath());
	}

}
