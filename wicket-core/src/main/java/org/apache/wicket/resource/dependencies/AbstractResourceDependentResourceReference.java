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

import java.util.Locale;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.Strings;

/**
 * A resource reference that also has resources that it is dependent on. For instance, you may have
 * foo.js that calls functions defined in base-utils.js. By creating a ResourceReference for foo.js,
 * your {@link IResourceReferenceDependencyConfigurationService} can create an
 * AbstractResourceDependentResourceReference that has base-utils.js as its dependency.
 * 
 * @see IResourceReferenceDependencyConfigurationService
 * @author Jeremy Thomerson
 */
public abstract class AbstractResourceDependentResourceReference extends ResourceReference
{

	private static final long serialVersionUID = 1L;

	/**
	 * The type of resource that an AbstractResourceDependentResourceReference represents.
	 * 
	 * @author Jeremy Thomerson
	 */
	public enum ResourceType {
		/**
		 * JavaScript reference
		 */
		JS,

		/**
		 * CSS reference
		 */
		CSS,

		/**
		 * plain text
		 */
		PLAIN;
	}

	private String uniqueId;
	private String media;

	/**
	 * Construct.
	 * 
	 * @param scope
	 * @param name
	 * @param locale
	 * @param style
	 * @param variation
	 */
	public AbstractResourceDependentResourceReference(Class<?> scope, String name, Locale locale,
		String style, String variation)
	{
		super(scope, name, locale, style, variation);
	}

	/**
	 * Construct.
	 * 
	 * @param scope
	 * @param name
	 */
	public AbstractResourceDependentResourceReference(Class<?> scope, String name)
	{
		super(scope, name);
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public AbstractResourceDependentResourceReference(String name)
	{
		super(name);
	}

	/**
	 * A globally-unique (to your application(s)) ID for this resource reference. This is optional,
	 * and is set for you if you call IHeaderResponse.renderJavaScriptReference(yourReference,
	 * "some-id").
	 * 
	 * @param uniqueId
	 */
	public void setUniqueId(String uniqueId)
	{
		this.uniqueId = uniqueId;
	}

	/**
	 * A globally-unique (to your application(s)) ID for this resource reference. This is optional,
	 * and is set for you if you call IHeaderResponse.renderJavaScriptReference(yourReference,
	 * "some-id").
	 * 
	 * @return globally-unique (to your application(s)) ID for this resource reference
	 */
	public String getUniqueId()
	{
		return uniqueId;
	}

	/**
	 * If you call
	 * ResourceReferenceDependencyInjectingHeaderResponse.renderCSSReference(yourReference,
	 * "some-media"), the media is set in this field so that it can later render itself properly to
	 * the wrapped IHeaderResponse
	 * 
	 * @param media
	 */
	public void setMedia(String media)
	{
		this.media = media;
	}

	/**
	 * If you call
	 * ResourceReferenceDependencyInjectingHeaderResponse.renderCSSReference(yourReference,
	 * "some-media"), the media is set in this field so that it can later render itself properly to
	 * the wrapped IHeaderResponse
	 * 
	 * @return the media set on calling
	 *         ResourceReferenceDependencyInjectingHeaderResponse.renderCSSReference(yourReference,
	 *         "some-media")
	 */
	public String getMedia()
	{
		return media;
	}

	/**
	 * Defaults to returning JS, but returns CSS if the name property ends with ".css".
	 * 
	 * You can override this method if you need more sophisticated behavior.
	 * 
	 * @return the ResourceType this reference represents, so that it can be properly added to the
	 *         {@link IHeaderResponse}
	 */
	public ResourceType getResourceType()
	{
		String resourceName = getName();

		final ResourceType type;
		if (Strings.isEmpty(resourceName))
		{
			type = ResourceType.PLAIN;
		}
		else if (resourceName.endsWith(".css"))
		{
			type = ResourceType.CSS;
		}
		else if (resourceName.endsWith(".js"))
		{
			type = ResourceType.JS;
		}
		else
		{
			throw new IllegalStateException("Cannot determine the resource's type by its name: " +
				resourceName);
		}
		return type;
	}

	/**
	 * Returns all ResourceReferences that this ResourceReference is depending on.
	 * 
	 * @return all ResourceReferences that this ResourceReference is depending on.
	 */
	public abstract AbstractResourceDependentResourceReference[] getDependentResourceReferences();

}
