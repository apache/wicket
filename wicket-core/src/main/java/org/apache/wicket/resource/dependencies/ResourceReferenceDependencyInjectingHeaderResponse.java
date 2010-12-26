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
package org.apache.wicket.resource.dependencies;

import org.apache.wicket.markup.html.DecoratingHeaderResponse;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderResponseDecorator;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.ResourceUtil;
import org.apache.wicket.resource.dependencies.AbstractResourceDependentResourceReference.ResourceType;

/**
 * A header response that can be used by {@link IHeaderResponseDecorator} to wrap another
 * IHeaderResponse. This response will take all references (js and css) and pass them to the
 * {@link IResourceReferenceDependencyConfigurationService} to get the dependency tree populated.
 * After this, it will call through to the wrapped header response and render the tree in the
 * child-first dependency order so that all dependencies are guaranteed to be satisfied before this
 * reference is rendered.
 * 
 * @author Jeremy Thomerson
 */
public class ResourceReferenceDependencyInjectingHeaderResponse extends DecoratingHeaderResponse
{
	private final IResourceReferenceDependencyConfigurationService configurationService;

	/**
	 * If you call this constructor, you MUST override getConfigurationService().
	 * 
	 * @param decorated
	 *            the response you are decorating
	 */
	public ResourceReferenceDependencyInjectingHeaderResponse(IHeaderResponse decorated)
	{
		this(decorated, null);
	}

	/**
	 * Construct this header response with a configuration service that will be used to find
	 * dependencies.
	 * 
	 * @param decorated
	 *            the response you are decorating
	 * @param configurator
	 *            the configuration service
	 */
	public ResourceReferenceDependencyInjectingHeaderResponse(IHeaderResponse decorated,
		IResourceReferenceDependencyConfigurationService configurator)
	{
		super(decorated);
		configurationService = configurator;
	}

	/**
	 * If you did not supply a non-null configuration service in the constructor, you must override
	 * this to return a configuration service to be used by this response.
	 * 
	 * @return the configuration service used by this response
	 */
	public IResourceReferenceDependencyConfigurationService getConfigurationService()
	{
		if (configurationService == null)
		{
			throw new IllegalStateException(
				"you must either provide an IResourceReferenceDependencyConfigurationService at construction time or override getConfigurationService()");
		}
		return configurationService;
	}

	/* overridden IHeaderResponse methods: */
	@Override
	public void renderCSSReference(ResourceReference reference)
	{
		render(get(reference));
	}

	@Override
	public void renderCSSReference(ResourceReference reference, String media)
	{
		AbstractResourceDependentResourceReference parent = get(reference);
		parent.setMedia(media);
		render(parent);
	}

	@Override
	public void renderJavaScriptReference(ResourceReference reference)
	{
		render(get(reference));
	}

	@Override
	public void renderJavaScriptReference(ResourceReference reference, String id)
	{
		AbstractResourceDependentResourceReference parent = get(reference);
		parent.setUniqueId(id);
		render(parent);
	}

	/**
	 * The method responsible for rendering the resource references. This should happen child-first
	 * so that the dependencies (children) of the given parent are loaded first, thereby satisfying
	 * the dependencies of the supplied parent.
	 * 
	 * Typically the actual rendering will happen on the response that this response is wrapping.
	 * The default implementation does all rendering to the wrapped response.
	 * 
	 * The default implementation delegates the actual rendering to the
	 * AbstractResourceDependentResourceReference itself.
	 * 
	 * @param parent
	 *            the reference that needs itself and all dependencies to be rendered
	 */
	protected void render(AbstractResourceDependentResourceReference parent)
	{
		for (AbstractResourceDependentResourceReference child : parent.getDependentResourceReferences())
		{
			render(child);
		}
		boolean css = ResourceType.CSS.equals(parent.getResourceType());
		String string = css ? parent.getMedia() : parent.getUniqueId();
		ResourceUtil.renderTo(getRealResponse(), parent, css, string);
	}

	private AbstractResourceDependentResourceReference get(ResourceReference reference)
	{
		AbstractResourceDependentResourceReference ref = getConfigurationService().configure(
			reference);
		if (ref == null)
		{
			throw new IllegalStateException(
				"your IResourceReferenceDependencyConfigurationService can not return null from configure");
		}
		return ref;
	}

}
