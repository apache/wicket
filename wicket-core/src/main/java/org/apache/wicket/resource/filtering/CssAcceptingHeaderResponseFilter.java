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
package org.apache.wicket.resource.filtering;

import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.dependencies.AbstractResourceDependentResourceReference;
import org.apache.wicket.resource.dependencies.AbstractResourceDependentResourceReference.ResourceType;
import org.apache.wicket.util.string.Strings;

/**
 * This filter accepts anything that appears to be CSS. All CSS that is not a resource reference (
 * {@link #acceptOtherCss()}) is accepted. All JS that is not a resource reference (
 * {@link #acceptOtherJavaScript()}) is not accepted.
 * 
 * The references are accepted if they appear to be CSS. If the reference passed in is an instance
 * of {@link AbstractResourceDependentResourceReference}, we use the {@link ResourceType} from it to
 * determine if it is CSS. Otherwise, we see if the ResourceReference.name property ends with
 * ".css".
 * 
 * @author Jeremy Thomerson
 */
public class CssAcceptingHeaderResponseFilter extends AbstractHeaderResponseFilter
{

	/**
	 * Construct.
	 * 
	 * @param name
	 *            name of the filter (used by the container that renders these resources)
	 */
	public CssAcceptingHeaderResponseFilter(String name)
	{
		super(name);
	}

	@Override
	public boolean acceptReference(ResourceReference ref)
	{
		if (ref instanceof AbstractResourceDependentResourceReference)
		{
			return ResourceType.CSS.equals(((AbstractResourceDependentResourceReference)ref).getResourceType());
		}
		if (!Strings.isEmpty(ref.getName()) && ref.getName().endsWith(".css"))
		{
			return true;
		}
		return false;
	}

	@Override
	public boolean acceptOtherJavaScript()
	{
		return false;
	}

}
