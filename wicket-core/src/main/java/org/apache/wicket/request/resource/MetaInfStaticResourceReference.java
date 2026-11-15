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
package org.apache.wicket.request.resource;

import java.util.List;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.util.string.Strings;

/**
 * Resource reference for static files. The resource must reside under the "/META-INF/resources/"
 * directory. So if you have a foo.bar.Component and want to have a static icon.gif belonging to it
 * the image must be available on the "META-INF/resources/foo/bar/icon.gif" classpath inside a jar
 * file.
 * 
 * If run under a Servlet 3.0 environment "foo/bar/icon.gif" like resource urls will be made and
 * served by the servlet container instead of wicket (which is faster).
 * 
 * If run under a non Servlet 3.0 environment (like 2.5) resources will be served by wicket (urls
 * will look like "wicket/resource/foo/bar/icon.gif").
 * 
 * @author akiraly
 */
public class MetaInfStaticResourceReference extends PackageResourceReference
{
	private static final long serialVersionUID = -1858339228780709471L;

	private static Boolean META_INF_RESOURCES_SUPPORTED = null;

	/**
	 * Construct.
	 * 
	 * @param scope
	 *            mandatory parameter
	 * @param name
	 *            mandatory parameter
	 */
	public MetaInfStaticResourceReference(Class<?> scope, String name)
	{
		super(scope, name);
	}

	/**
	 * Returns the {@link Url} for given {@link IRequestHandler} if "/META-INF/resources" Servlet
	 * 3.0 feature is supported or <code>null</code> if not (so standard url mapping can take
	 * place).
	 * 
	 * @param requestHandler
	 *            mandatory parameter
	 * @return Url instance or <code>null</code>.
	 */
	public Url mapHandler(IRequestHandler requestHandler)
	{
		if (!isMetaInfResourcesSupported())
		{
			return null;
		}

		Url url = new Url();

		List<String> segments = url.getSegments();

		String packageName = Packages.extractPackageName(getScope());
		String[] parts = Strings.split(packageName, '.');
		for (String p : parts)
		{
			segments.add(p);
		}

		parts = Strings.split(getName(), '/');
		for (String p : parts)
		{
			segments.add(p);
		}

		return url;
	}

	protected boolean isMetaInfResourcesSupported()
	{
		if (META_INF_RESOURCES_SUPPORTED == null)
		{
			int majorVersion = WebApplication.get().getServletContext().getMajorVersion();
			META_INF_RESOURCES_SUPPORTED = majorVersion >= 3;
		}

		return META_INF_RESOURCES_SUPPORTED;
	}

}
