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
package org.apache.wicket.resource;

import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.WebExternalResourceStream;

/**
 * Resource served from a file relative to the context root.
 * 
 * @author almaw
 */
public class ContextRelativeResource extends WebResource
{
	private static final long serialVersionUID = 1L;

	private final String path;

	/**
	 * Construct.
	 * 
	 * @param pathRelativeToContextRoot
	 */
	public ContextRelativeResource(String pathRelativeToContextRoot)
	{
		if (pathRelativeToContextRoot == null)
		{
			throw new IllegalArgumentException("Cannot have null path for ContextRelativeResource.");
		}

		// Make sure there is a leading '/'.
		if (!pathRelativeToContextRoot.startsWith("/"))
		{
			pathRelativeToContextRoot = "/" + pathRelativeToContextRoot;
		}
		this.path = pathRelativeToContextRoot;
	}

	/**
	 * @see org.apache.wicket.Resource#getResourceStream()
	 */
	public IResourceStream getResourceStream()
	{
		return new WebExternalResourceStream(path);
	}

}
