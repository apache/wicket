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

import java.util.Locale;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.IClusterable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceReferenceRegistry;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.apache.wicket.util.template.TextTemplate;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;

/**
 * A class which adapts a {@link PackageTextTemplate} to a {@link ResourceReference}.
 * 
 * @see {@link "https://cwiki.apache.org/WICKET/dynamically-generate-a-css-stylesheet.html"}
 * 
 * @author James Carman
 */
public class TextTemplateResourceReference extends ResourceReference implements IClusterable
{

	private static final long serialVersionUID = 1L;

	private final TextTemplate textTemplate;
	private final IModel<Map<String, Object>> variablesModel;
	private final ResourceStreamResource resource;

	/**
	 * Creates a resource reference to a {@link PackageTextTemplate}.
	 * 
	 * @param scope
	 *            the <code>Class</code> to be used for retrieving the classloader for loading the
	 *            <code>PackagedTextTemplate</code>
	 * @param fileName
	 *            the file name
	 * @param variablesModel
	 *            the template variables as a model
	 */
	public TextTemplateResourceReference(final Class<?> scope, final String fileName,
		IModel<Map<String, Object>> variablesModel)
	{
		this(scope, fileName, PackageTextTemplate.DEFAULT_CONTENT_TYPE,
			PackageTextTemplate.DEFAULT_ENCODING, variablesModel);
	}

	/**
	 * Creates a resource reference to a {@link PackageTextTemplate}.
	 * 
	 * @param scope
	 *            the <code>Class</code> to be used for retrieving the classloader for loading the
	 *            <code>PackagedTextTemplate</code>
	 * @param fileName
	 *            the file name
	 * @param contentType
	 *            the mime type of this resource, such as "<code>image/jpeg</code>" or "
	 *            <code>text/html</code>"
	 * @param variablesModel
	 *            the template variables as a model
	 */
	public TextTemplateResourceReference(final Class<?> scope, final String fileName,
		final String contentType, IModel<Map<String, Object>> variablesModel)
	{
		this(scope, fileName, contentType, PackageTextTemplate.DEFAULT_ENCODING, variablesModel);
	}

	/**
	 * Creates a resource reference to a {@link PackageTextTemplate}.
	 * 
	 * @param scope
	 *            the <code>Class</code> to be used for retrieving the classloader for loading the
	 *            <code>PackagedTextTemplate</code>
	 * @param fileName
	 *            the file name
	 * @param contentType
	 *            the mime type of this resource, such as "<code>image/jpeg</code>" or "
	 *            <code>text/html</code>"
	 * @param encoding
	 *            the file's encoding, for example, "<code>UTF-8</code>"
	 * @param variablesModel
	 *            the template variables as a model
	 */
	public TextTemplateResourceReference(final Class<?> scope, final String fileName,
		final String contentType, final String encoding, IModel<Map<String, Object>> variablesModel)
	{
		this(scope, fileName, contentType, encoding, variablesModel, null, null, null);
	}

	/**
	 * Construct.
	 * 
	 * @param scope
	 *            the <code>Class</code> to be used for retrieving the classloader for loading the
	 *            <code>PackagedTextTemplate</code>
	 * @param fileName
	 *            the file name
	 * @param contentType
	 *            the mime type of this resource, such as "<code>image/jpeg</code>" or "
	 *            <code>text/html</code>"
	 * @param encoding
	 *            the file's encoding, for example, "<code>UTF-8</code>"
	 * @param variablesModel
	 *            the template variables as a model
	 * @param locale
	 *            Preferred locale for the resource
	 * @param style
	 *            Preferred style for the resource
	 * @param variation
	 *            Preferred variation for the resource
	 */
	public TextTemplateResourceReference(final Class<?> scope, final String fileName,
		final String contentType, final String encoding,
		IModel<Map<String, Object>> variablesModel, Locale locale, String style, String variation)
	{
		super(scope, fileName, locale, style, variation);

		textTemplate = new PackageTextTemplate(scope, fileName, contentType, encoding);
		this.variablesModel = variablesModel;

		resource = new ResourceStreamResource(null)
		{
			@Override
			protected IResourceStream getResourceStream()
			{
				IModel<Map<String, Object>> variables = TextTemplateResourceReference.this.variablesModel;
				String stringValue = textTemplate.asString(variables.getObject());
				variables.detach(); // We're done with the model so detach it!

				StringResourceStream resourceStream = new StringResourceStream(stringValue,
						textTemplate.getContentType());
				resourceStream.setLastModified(Time.now());

				return resourceStream;
			}
		};
		resource.setCacheDuration(Duration.NONE);

		if (Application.exists())
		{
			// TextTemplateResourceReference should not be cached due to its dynamic nature
			// Old entry in the registry would keep wrong 'variablesModel'
			ResourceReferenceRegistry resourceReferenceRegistry = Application.get().getResourceReferenceRegistry();
			resourceReferenceRegistry.unregisterResourceReference(getKey());
			resourceReferenceRegistry.registerResourceReference(this);
		}
	}

	/**
	 * Creates a new resource which returns the interpolated value of the text template.
	 * 
	 * @return a new resource which returns the interpolated value of the text template
	 */
	@Override
	public IResource getResource()
	{
		return resource;
	}
}
