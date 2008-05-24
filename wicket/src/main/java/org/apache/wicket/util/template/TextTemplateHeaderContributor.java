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

import java.util.Map;

import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;


/**
 * A header contributor that will contribute the contents of the given template interpolated with
 * the provided <code>Map</code> of variables.
 * 
 * @author Eelco Hillenius
 * @since 1.2.6
 */
public class TextTemplateHeaderContributor extends StringHeaderContributor
{
	private static final long serialVersionUID = 1L;

	/**
	 * This model holds the template and returns the interpolation of the template.
	 */
	private static final class TemplateModel extends LoadableDetachableModel<String>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * The template to operate on and which makes the contribution.
		 */
		private final TextTemplate template;

		/**
		 * The <code>IModel</code> that holds any variables for interpolation. It should return a
		 * {@link Map} or <code>null</code>.
		 */
		private final IModel<Map<String, Object>> variablesModel;

		/**
		 * Constructor.
		 * 
		 * @param template
		 *            the <code>TextTemplate</code> to work on
		 * @param variablesModel
		 *            the <code>IModel</code> that holds any variables for interpolation. It
		 *            should return a {@link Map} or <code>null</code>.
		 */
		protected TemplateModel(TextTemplate template, IModel<Map<String, Object>> variablesModel)
		{
			if (template == null)
			{
				throw new IllegalArgumentException("argument template must be not null");
			}

			this.template = template;
			this.variablesModel = variablesModel;
		}

		/**
		 * @see org.apache.wicket.model.IModel#detach()
		 */
		@Override
		public void detach()
		{
			if (variablesModel != null)
			{
				variablesModel.detach();
			}
			super.detach();
		}

		@Override
		protected String load()
		{
			if (variablesModel != null)
			{
				Map<String, Object> variables = variablesModel.getObject();
				if (variables != null)
				{
					return template.asString(variables);
				}
			}
			return template.asString();
		}
	}

	/**
	 * Retrieves a CSS header contributor based on the given <code>TextTemplate</code>. The
	 * template will be interpolated with the given variables. The content will be written as the
	 * body of a script/tag pair.
	 * 
	 * @param template
	 *            the <code>TextTemplate</code> that is the base for the contribution
	 * @param variablesModel
	 *            the variables to interpolate
	 * @return the <code>TextTemplateHeaderContributor</code> instance
	 */
	public static TextTemplateHeaderContributor forCss(TextTemplate template,
		IModel<Map<String, Object>> variablesModel)
	{
		return new TextTemplateHeaderContributor(new CssTemplate(template), variablesModel);
	}

	/**
	 * Retrieves a CSS header contributor that will load the template from the given file name
	 * relative to (or in the same package as) the provided <code>clazz</code> argument. The
	 * template will be interpolated with the given variables. The content will be written as the
	 * body of a script/tag pair.
	 * 
	 * @param clazz
	 *            the <code>Class</code> to be used for retrieving the classloader for loading the
	 *            packaged template.
	 * @param fileName
	 *            the name of the file, relative to the <code>clazz</code> position
	 * @param variablesModel
	 *            the variables to interpolate
	 * @return the <code>TextTemplateHeaderContributor</code> instance
	 */
	public static TextTemplateHeaderContributor forCss(final Class<?> clazz, final String fileName,
		IModel<Map<String, Object>> variablesModel)
	{
		return forCss(new PackagedTextTemplate(clazz, fileName), variablesModel);
	}

	/**
	 * Retrieves a JavaScript header contributor based on the given <code>TextTemplate</code>.
	 * The template will be interpolated with the given variables. The content will be written as
	 * the body of a script/tag pair.
	 * 
	 * @param template
	 *            the <code>TextTemplate</code> that is the base for the contribution
	 * @param variablesModel
	 *            the variables to interpolate
	 * @return the <code>TextTemplateHeaderContributor</code> instance
	 */
	public static TextTemplateHeaderContributor forJavaScript(TextTemplate template,
		IModel<Map<String, Object>> variablesModel)
	{
		return new TextTemplateHeaderContributor(new JavaScriptTemplate(template), variablesModel);
	}

	/**
	 * Retrieves a JavaScript header contributor that will load the template from the given file
	 * name relative to (or in the same package as) the provided <code>clazz</code> argument. The
	 * template will be interpolated with the given variables. The content will be written as the
	 * body of a script/tag pair.
	 * 
	 * @param clazz
	 *            the <code>Class</code> to be used for retrieving the classloader for loading the
	 *            packaged template.
	 * @param fileName
	 *            the name of the file, relative to the <code>clazz</code> position
	 * @param variablesModel
	 *            the variables to interpolate
	 * @return the <code>TextTemplateHeaderContributor</code> instance
	 */
	public static TextTemplateHeaderContributor forJavaScript(final Class<?> clazz,
		final String fileName, IModel<Map<String, Object>> variablesModel)
	{
		return forJavaScript(new PackagedTextTemplate(clazz, fileName), variablesModel);
	}

	/**
	 * Constructor.
	 * 
	 * @param template
	 *            the <code>TextTemplate</code> with the contribution
	 * @param variablesModel
	 *            optional <code>IModel</code> for variable substitution
	 */
	protected TextTemplateHeaderContributor(TextTemplate template,
		IModel<Map<String, Object>> variablesModel)
	{
		super(new TemplateModel(template, variablesModel));
	}
}