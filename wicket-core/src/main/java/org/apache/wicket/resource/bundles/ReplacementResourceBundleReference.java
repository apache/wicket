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
package org.apache.wicket.resource.bundles;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * An extension of ResourceBundleReference that is used especially
 * for resource replacements
 *
 * @see org.apache.wicket.protocol.http.WebApplication#addResourceReplacement(org.apache.wicket.request.resource.CssResourceReference, org.apache.wicket.request.resource.ResourceReference)
 * @see org.apache.wicket.protocol.http.WebApplication#addResourceReplacement(org.apache.wicket.request.resource.JavaScriptResourceReference, org.apache.wicket.request.resource.ResourceReference)
 * @since 6.13
 */
public class ReplacementResourceBundleReference extends ResourceBundleReference
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new bundle reference from the given reference.
	 *
	 * @param bundleRef
	 */
	public ReplacementResourceBundleReference(ResourceReference bundleRef)
	{
		super(bundleRef);
	}
	
	/**
	 * Returns the dependencies of the replacing resource reference.
	 * 
	 * @return the dependencies of the replacing resource reference
	 */
	@Override
	public List<HeaderItem> getDependencies() 
	{
		return getBundleReference().getDependencies();
	}
}
