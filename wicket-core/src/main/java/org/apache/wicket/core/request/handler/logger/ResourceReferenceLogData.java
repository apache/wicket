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
package org.apache.wicket.core.request.handler.logger;

import org.apache.wicket.request.handler.resource.ResourceLogData;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Contains logging data for resource reference requests.
 *
 * @author Emond Papegaaij
 */
public class ResourceReferenceLogData extends ResourceLogData
{
	private static final long serialVersionUID = 1L;

	private final Class<? extends ResourceReference> resourceReferenceClass;
	private final Class<?> scope;
	private final PageParameters pageParameters;

	/**
	 * Construct.
	 *
	 * @param refHandler
	 */
	public ResourceReferenceLogData(ResourceReferenceRequestHandler refHandler)
	{
		super(refHandler.getResourceReference().getName(), refHandler.getLocale(),
			refHandler.getStyle(), refHandler.getVariation());
		resourceReferenceClass = refHandler.getResourceReference().getClass();
		scope = refHandler.getResourceReference().getScope();
		pageParameters = refHandler.getPageParameters();
	}

	/**
	 * @return resourceReferenceClass
	 */
	public final Class<? extends ResourceReference> getResourceReferenceClass()
	{
		return resourceReferenceClass;
	}

	/**
	 * @return scope
	 */
	public final Class<?> getScope()
	{
		return scope;
	}

	/***
	 * @return pageParameters
	 */
	public final PageParameters getPageParameters()
	{
		return pageParameters;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("{");
		fillToString(sb);
		sb.append(",resourceReferenceClass=");
		sb.append(getResourceReferenceClass().getName());
		sb.append(",scope=");
		sb.append(getScope() == null ? "null" : getScope().getName());
		sb.append(",pageParameters={");
		sb.append(getPageParameters());
		sb.append("}}");
		return sb.toString();
	}
}
