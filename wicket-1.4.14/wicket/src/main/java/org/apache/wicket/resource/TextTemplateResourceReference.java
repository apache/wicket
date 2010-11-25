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

import java.util.Map;

import org.apache.wicket.IClusterable;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.template.PackagedTextTemplate;
import org.apache.wicket.util.template.TextTemplate;
import org.apache.wicket.util.time.Time;

/**
 * A class which adapts a {@link PackagedTextTemplate} to a {@link ResourceReference}.
 * 
 * @see {@link https://cwiki.apache.org/WICKET/dynamically-generate-a-css-stylesheet.html}
 * 
 * @author James Carman
 */
public class TextTemplateResourceReference extends ResourceReference implements IClusterable
{
// **********************************************************************************************************************
// Fields
// **********************************************************************************************************************

	private static final long serialVersionUID = 1L;
	private final TextTemplate textTemplate;
	private final IModel<Map<String, Object>> variablesModel;

// **********************************************************************************************************************
// Constructors
// **********************************************************************************************************************

	/**
	 * Creates a resource reference to a {@link PackagedTextTemplate}.
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
		super(scope, fileName);

		textTemplate = new PackagedTextTemplate(scope, fileName);
		this.variablesModel = variablesModel;
	}

	/**
	 * Creates a resource reference to a {@link PackagedTextTemplate}.
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
		super(scope, fileName);

		textTemplate = new PackagedTextTemplate(scope, fileName, contentType);
		this.variablesModel = variablesModel;
	}

	/**
	 * Creates a resource reference to a {@link PackagedTextTemplate}.
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
		super(scope, fileName);

		textTemplate = new PackagedTextTemplate(scope, fileName, contentType, encoding);
		this.variablesModel = variablesModel;
	}

// **********************************************************************************************************************
// Other Methods
// **********************************************************************************************************************

	/**
	 * Creates a new resource which returns the interpolated value of the text template.
	 * 
	 * @return a new resource which returns the interpolated value of the text template
	 */
	@Override
	protected Resource newResource()
	{
		return new Resource()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public IResourceStream getResourceStream()
			{
				final String stringValue = textTemplate.asString(variablesModel.getObject());
				variablesModel.detach(); // We're done with the model so detach it!
				return new StringResourceStream(stringValue, textTemplate.getContentType());
			}
		};
	}

	/**
	 * Returns the last modified time of the {@link PackagedTextTemplate} itself.
	 * 
	 * @return the last modified time of the {@link PackagedTextTemplate} itself
	 */
	@Override
	public Time lastModifiedTime()
	{
		return textTemplate.lastModifiedTime();
	}
}
