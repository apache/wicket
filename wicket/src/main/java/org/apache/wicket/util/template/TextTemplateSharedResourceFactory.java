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
package org.apache.wicket.util.template;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.SharedResources;
import org.apache.wicket.util.resource.AbstractStringResourceStream;
import org.apache.wicket.util.resource.IResourceStream;


/**
 * A resource (reference) factory that takes a <code>TextTemplate</code> and generates shared
 * resources for various interpolations of that template.
 * <p>
 * A scope for adding shared resources allows you to limit the namespace impact of the shared
 * resources created. If you omit the scope, the application-wide scope
 * <code>Application.class</code> will be used by default.
 * <p>
 * You may use resources created by this factory directly by calling
 * <code>resourceReference(Map)</code> to get a resource reference to the given shared resource
 * interpolation represented by the variables in the <code>Map</code>. Or, for convenience, you
 * can use <code>TextTemplateLink</code> to link to resources created by this factory.
 * <p>
 * In many cases, it will be useful to extend this class and override
 * <code>sharedResourceName(Map)</code> in order to provide a unique name for resources created by
 * the factory using map values. If you don't provide an override, every value in the map will be
 * used to produce the unique name, which may create either longer names or more unique shared
 * resources than you really wanted.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
// TODO Should weak-ref regenerable resources like this in SharedResources!
public class TextTemplateSharedResourceFactory
{
	private static final long serialVersionUID = 1L;

	/**
	 * Shared resource scope.
	 */
	private final WeakReference<Class<?>> scopeRef;

	/**
	 * <code>TextTemplate</code> to use to create resources.
	 */
	private final TextTemplate template;

	/**
	 * Creates shared <code>TextTemplate</code> resources.
	 * 
	 * @param template
	 *            the <code>TextTemplate</code> to interpolate into
	 */
	public TextTemplateSharedResourceFactory(final TextTemplate template)
	{
		this(template, Application.class);
	}

	/**
	 * Creates shared <code>TextTemplate</code> resources with the given scope.
	 * 
	 * @param template
	 *            the <code>TextTemplate</code> to interpolate into
	 * @param scope
	 *            the scope in shared resources at which to add resources
	 */
	public TextTemplateSharedResourceFactory(final TextTemplate template, final Class<?> scope)
	{
		this.template = template;
		scopeRef = new WeakReference<Class<?>>(scope);
	}

	/**
	 * Interpolates the given variables <code>Map</code> and returns a
	 * <code>ResourceReference</code>.
	 * 
	 * @param variables
	 *            the variables to interpolate into the template
	 * @return a <code>ResourceReference</code> to the template encoded as a resource with the
	 *         given variables interpolated
	 */
	public ResourceReference resourceReference(final Map<String, Object> variables)
	{
		final String uniqueName = sharedResourceName(variables);
		final String templateValue = template.asString(variables);
		final SharedResources sharedResources = Application.get().getSharedResources();
		final Resource resource = sharedResources.get(uniqueName);
		if (resource == null)
		{
			final Resource newResource = new Resource()
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see org.apache.wicket.Resource#getResourceStream()
				 */
				@Override
				public IResourceStream getResourceStream()
				{
					return new AbstractStringResourceStream()
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected String getString()
						{
							return templateValue;
						}

						@Override
						public long length()
						{
							return templateValue.length();
						}
					};
				}
			};
			sharedResources.add(uniqueName, newResource);
		}
		return new ResourceReference(scopeRef.get(), uniqueName);
	}

	/**
	 * Returns a unique name for the variables to use as a resource key.
	 * 
	 * @param variables
	 *            variables that parameterize the linked-to resource
	 * @return a unique name for the variables to use as a resource key
	 */
	protected String sharedResourceName(final Map<String, Object> variables)
	{
		final StringBuffer buffer = new StringBuffer();
		for (final Iterator<Object> iterator = variables.values().iterator(); iterator.hasNext();)
		{
			final String value = iterator.next().toString();
			buffer.append(encodeValue(value));
			if (iterator.hasNext())
			{
				buffer.append('-');
			}
		}
		return buffer.toString();
	}

	/**
	 * Simple encoder for key values. Letters and digits are unchanged. All others are encoded as %<hexcode>.
	 * 
	 * @param value
	 *            a value
	 * @return the encoded value
	 */
	private String encodeValue(final String value)
	{
		final StringBuffer buffer = new StringBuffer(value.length() + 10);
		for (int i = 0; i < value.length(); i++)
		{
			final char c = value.charAt(i);
			if (Character.isLetterOrDigit(c))
			{
				buffer.append(c);
			}
			else
			{
				buffer.append('%');
				buffer.append(Integer.toHexString(c));
			}
		}
		return buffer.toString();
	}
}
