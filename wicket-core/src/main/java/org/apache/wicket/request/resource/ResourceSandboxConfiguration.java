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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.lang.Args;

/**
 * Configures whether {@link AbstractResource} marks its responses as content that must not run in
 * the origin of the application. When enabled, a resource response carries
 * <code>Content-Security-Policy: sandbox</code>, and additionally
 * <code>X-Content-Type-Options: nosniff</code> if it has a content type. A browser then renders
 * an HTML or SVG document served by a resource in an opaque origin without running its scripts,
 * and takes the declared content type instead of guessing one. This is meant for applications
 * that serve files they did not author, such as uploads.
 * <p>
 * The configuration is disabled by default and lives in
 * {@link org.apache.wicket.settings.SecuritySettings}. Set it up in the <code>init()</code>
 * method of the application, it is read for every resource response:
 *
 * <pre>
 * &#064;Override
 * protected void init()
 * {
 * 	// ...
 * 	getSecuritySettings()
 * 		.setResourceSandboxConfiguration(true, "/wicket/resource/com.example.Workers")
 * 		.getResourceSandboxConfiguration()
 * 		.addExemptedResource(ReportResource.class);
 * 	// ...
 * }
 * </pre>
 *
 * What is and is not covered:
 * <ul>
 * <li>Only resources extending {@link AbstractResource} are covered, whichever request handler
 * serves them. An {@link IResource} implemented from scratch has to set the headers itself.</li>
 * <li>A response without a content type gets no <code>nosniff</code>, because a browser refuses
 * to use such a response as a script or a stylesheet.</li>
 * <li>A resource that sets a <code>Content-Security-Policy</code> header of its own through
 * {@link AbstractResource.ResourceResponse#getHeaders()} keeps exactly that policy,
 * <code>sandbox</code> is not added to it. The same goes for
 * <code>X-Content-Type-Options</code>.</li>
 * <li>A <code>304 Not Modified</code> or an error response gets neither header.</li>
 * <li>Exempted resources are served unchanged. An exemption is a decision about trust: exempt
 * only content the application authored, such as a worker script or an HTML resource that has to
 * run its own scripts.</li>
 * </ul>
 *
 * @see org.apache.wicket.settings.SecuritySettings#setResourceSandboxConfiguration(boolean,
 *      String...)
 * @since 12.0.0
 */
public class ResourceSandboxConfiguration
{
	/**
	 * The name of the header that carries {@link #SANDBOX}.
	 *
	 * @since 12.0.0
	 */
	public static final String CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy";

	/**
	 * The policy that gives a document an opaque origin and keeps its scripts from running.
	 *
	 * @since 12.0.0
	 */
	public static final String SANDBOX = "sandbox";

	/**
	 * The name of the header that carries {@link #NOSNIFF}.
	 *
	 * @since 12.0.0
	 */
	public static final String CONTENT_TYPE_OPTIONS_HEADER = "X-Content-Type-Options";

	/**
	 * The value that makes a browser take the declared content type instead of guessing one.
	 *
	 * @since 12.0.0
	 */
	public static final String NOSNIFF = "nosniff";

	private final boolean enabled;

	private final Set<List<String>> exemptedPaths = new HashSet<>();

	private final Set<Class<? extends IResource>> exemptedResources = new HashSet<>();

	/**
	 * @param enabled
	 *            whether resource responses get the headers
	 * @param exemptedPaths
	 *            see {@link #addExemptedPath(String)}
	 * @since 12.0.0
	 */
	public ResourceSandboxConfiguration(boolean enabled, String... exemptedPaths)
	{
		this.enabled = enabled;
		for (String path : exemptedPaths)
		{
			addExemptedPath(path);
		}
	}

	/**
	 * Exempts the resources requested with the given path or a path below it. The path is
	 * relative to the Wicket filter, as in <code>WebApplication#mountResource()</code>, and is
	 * compared segment by segment with the path of the request URL, so
	 * <code>/files</code> exempts <code>/files/a.html</code> but not <code>/files2</code>. The
	 * file name of a package resource carries a version by default, so exempt the directory of
	 * its scope or mount the resource. A resource requested through a component has no path of
	 * its own, see {@link #addExemptedResource(Class)}.
	 *
	 * @param path
	 *            the path to exempt
	 * @return {@code this} object for chaining
	 * @since 12.0.0
	 */
	public ResourceSandboxConfiguration addExemptedPath(String path)
	{
		exemptedPaths.add(Args.notEmpty(segments(Url.parse(Args.notNull(path, "path"))), "path"));
		return this;
	}

	/**
	 * Exempts all resources that are an instance of the given class, however they are requested.
	 *
	 * @param resourceClass
	 *            the class of the resources to exempt
	 * @return {@code this} object for chaining
	 * @since 12.0.0
	 */
	public ResourceSandboxConfiguration addExemptedResource(
		Class<? extends IResource> resourceClass)
	{
		exemptedResources.add(Args.notNull(resourceClass, "resourceClass"));
		return this;
	}

	/**
	 * @return the exempted paths, without leading and trailing slash
	 * @since 12.0.0
	 */
	public Set<String> getExemptedPaths()
	{
		return exemptedPaths.stream()
			.map(segments -> String.join("/", segments))
			.collect(Collectors.toSet());
	}

	/**
	 * @return the exempted resource classes
	 * @since 12.0.0
	 */
	public Set<Class<? extends IResource>> getExemptedResources()
	{
		return exemptedResources;
	}

	/**
	 * @return whether resource responses get the headers
	 * @since 12.0.0
	 */
	public boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * @param resource
	 *            the resource that is about to respond
	 * @param request
	 *            the request it responds to
	 * @return whether the resource is exempted by its class or by the path of the request
	 * @since 12.0.0
	 */
	public boolean isExempted(IResource resource, Request request)
	{
		for (Class<? extends IResource> resourceClass : exemptedResources)
		{
			if (resourceClass.isInstance(resource))
			{
				return true;
			}
		}
		if (exemptedPaths.isEmpty())
		{
			return false;
		}
		List<String> path = segments(request.getUrl());
		for (List<String> exemptedPath : exemptedPaths)
		{
			if (path.size() >= exemptedPath.size() &&
				path.subList(0, exemptedPath.size()).equals(exemptedPath))
			{
				return true;
			}
		}
		return false;
	}

	private static List<String> segments(Url url)
	{
		List<String> segments = new ArrayList<>(url.canonical().getSegments());
		segments.removeIf(String::isEmpty);
		return segments;
	}
}
