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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.Application;
import org.apache.wicket.ResourceBundles;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IReferenceHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;

/**
 * A resource bundle that automatically concatenates the given resources. These resources should all
 * be of the same type (javascript or css) and all have {@link PackageResourceReference} (or
 * subclasses). After creating the bundle, you normally have to register it in the
 * {@link ResourceBundles} under {@link Application#getResourceBundles()}. {@link ResourceBundles}
 * has two utility methods to create instances of this class:
 * {@link ResourceBundles#addJavaScriptBundle(Class, String, JavaScriptResourceReference...)
 * addJavaScriptBundle} and
 * {@link ResourceBundles#addCssBundle(Class, String, CssResourceReference...) addCssBundle}.
 * Dependencies are inherited from the provided resources, if the bundle does not provide all
 * dependencies itself.
 * 
 * @author papegaaij
 * @param <T>
 *            The type of the header items to bundle
 */
public class ConcatResourceBundleReference<T extends HeaderItem & IReferenceHeaderItem> extends
	ResourceReference implements IResourceBundle
{
	private static final long serialVersionUID = 1L;

	private final List<T> providedResources;

	/**
	 * Creates a new {@link ConcatResourceBundleReference} for the given resources.
	 * 
	 * @param scope
	 * @param name
	 * @param resources
	 */
	public ConcatResourceBundleReference(Class<?> scope, String name, List<T> resources)
	{
		this(scope, name, null, null, null, resources);
	}

	/**
	 * Creates a new {@link ConcatResourceBundleReference} for the given resources.
	 * 
	 * @param scope
	 * @param name
	 * @param resources
	 */
	public ConcatResourceBundleReference(Class<?> scope, String name, T... resources)
	{
		this(scope, name, null, null, null, Arrays.asList(resources));
	}

	/**
	 * Creates a new {@link ConcatResourceBundleReference} for the given resources.
	 * 
	 * @param name
	 * @param resources
	 */
	public ConcatResourceBundleReference(String name, T... resources)
	{
		this(Application.class, name, null, null, null, Arrays.asList(resources));
	}

	/**
	 * Creates a new {@link ConcatResourceBundleReference} for the given resources.
	 * 
	 * @param scope
	 *            mandatory parameter
	 * @param name
	 *            mandatory parameter
	 * @param locale
	 *            resource locale
	 * @param style
	 *            resource style
	 * @param variation
	 *            resource variation
	 * @param resources
	 *            the resources that are concatenated
	 */
	public ConcatResourceBundleReference(Class<?> scope, String name, Locale locale, String style,
		String variation, List<T> resources)
	{
		super(scope, name, locale, style, variation);
		providedResources = Args.notNull(resources, "resources");
		checkProvidedResources();
	}

	/* check if all provided resources are package resources */
	private void checkProvidedResources()
	{
		for (T curProvidedResource : providedResources)
		{
			ResourceReference reference = curProvidedResource.getReference();
			if (!(reference instanceof CssResourceReference || reference instanceof JavaScriptResourceReference))
			{
				throw new IllegalArgumentException(
					"ConcatResourceBundleReference only works with CssResourceReference and JavaScriptResourceReference, " +
						curProvidedResource + " provides a " +
						reference.getClass().getSimpleName());
			}
		}
	}

	@Override
	public IResource getResource()
	{
		return new ConcatBundleResource(providedResources);
	}

	@Override
	public List<T> getProvidedResources()
	{
		return providedResources;
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies()
	{
		Set<HeaderItem> ret = new LinkedHashSet<HeaderItem>();
		for (HeaderItem curProvided : providedResources)
		{
			for (HeaderItem curDependency : curProvided.getDependencies())
				ret.add(curDependency);
		}
		for (HeaderItem curProvided : providedResources)
		{
			ret.remove(curProvided);
		}
		return ret;
	}
}
