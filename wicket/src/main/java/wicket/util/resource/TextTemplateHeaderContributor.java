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
package wicket.util.resource;

import java.util.Map;

import wicket.behavior.StringHeaderContributor;
import wicket.model.AbstractReadOnlyDetachableModel;
import wicket.model.IModel;

/**
 * A header contributor that will contribute the contents of the given template
 * interpolated with the provided map of variables.
 * 
 * @author Eelco Hillenius
 */
public class TextTemplateHeaderContributor extends StringHeaderContributor
{
	/**
	 * This model holds the template and returns the interpolation of the
	 * template with of any of the
	 */
	private static final class TemplateModel extends AbstractReadOnlyDetachableModel<String>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * The template to operate on and which makes the contribution.
		 */
		private final TextTemplate template;

		/**
		 * The model that holds any variables for interpolation. It should
		 * return a {@link Map} or null.
		 */
		private final IModel<Map> variablesModel;

		/**
		 * Construct.
		 * 
		 * @param template
		 *            the template to work on
		 * @param variablesModel
		 *            The model that holds any variables for interpolation. It
		 *            should return a {@link Map} or null.
		 */
		protected TemplateModel(TextTemplate template, IModel<Map> variablesModel)
		{
			if (template == null)
			{
				throw new IllegalArgumentException("argument template must be not null");
			}

			this.template = template;
			this.variablesModel = variablesModel;
		}

		/**
		 * @see wicket.model.AbstractDetachableModel#onAttach()
		 */
		@Override
		protected void onAttach()
		{
		}

		/**
		 * @see wicket.model.AbstractDetachableModel#onDetach()
		 */
		@Override
		protected void onDetach()
		{
			if (variablesModel != null)
			{
				variablesModel.detach();
			}
		}

		/**
		 * @see wicket.model.AbstractDetachableModel#onGetObject()
		 */
		@Override
		protected String onGetObject()
		{
			if (variablesModel != null)
			{
				Map variables = variablesModel.getObject();
				if (variables != null)
				{
					return template.asString(variables);
				}
			}
			return template.asString();
		}
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Gets a css header contributor that will load the template from the given
	 * file name relative to (/in the same package as) the provided clazz
	 * argument. The template will be interpolated with the given variables. The
	 * content will be written as the body of a script tag pair.
	 * 
	 * @param clazz
	 *            The class to be used for retrieving the classloader for
	 *            loading the packaged template.
	 * @param fileName
	 *            The name of the file, relative to the clazz position
	 * @param variablesModel
	 *            The variables to interpolate
	 * @return The header contributor instance
	 */
	public static TextTemplateHeaderContributor forCss(final Class clazz, final String fileName,
			IModel<Map> variablesModel)
	{
		return forCss(new PackagedTextTemplate(clazz, fileName), variablesModel);
	}

	/**
	 * Gets a css header contributor based on the given text template. The
	 * template will be interpolated with the given variables. The content will
	 * be written as the body of a script tag pair.
	 * 
	 * @param template
	 *            The text template that is the base for the contribution
	 * @param variablesModel
	 *            The variables to interpolate
	 * @return The header contributor instance
	 */
	public static TextTemplateHeaderContributor forCss(TextTemplate template,
			IModel<Map> variablesModel)
	{
		return new TextTemplateHeaderContributor(new CssTemplate(template), variablesModel);
	}

	/**
	 * Gets a javascript header contributor that will load the template from the
	 * given file name relative to (/in the same package as) the provided clazz
	 * argument. The template will be interpolated with the given variables. The
	 * content will be written as the body of a script tag pair.
	 * 
	 * @param clazz
	 *            The class to be used for retrieving the classloader for
	 *            loading the packaged template.
	 * @param fileName
	 *            The name of the file, relative to the clazz position
	 * @param variablesModel
	 *            The variables to interpolate
	 * @return The header contributor instance
	 */
	public static TextTemplateHeaderContributor forJavaScript(final Class clazz,
			final String fileName, IModel<Map> variablesModel)
	{
		return forJavaScript(new PackagedTextTemplate(clazz, fileName), variablesModel);
	}

	/**
	 * Gets a javascript header contributor based on the given text template.
	 * The template will be interpolated with the given variables. The content
	 * will be written as the body of a script tag pair.
	 * 
	 * @param template
	 *            The text template that is the base for the contribution
	 * @param variablesModel
	 *            The variables to interpolate
	 * @return The header contributor instance
	 */
	public static TextTemplateHeaderContributor forJavaScript(TextTemplate template,
			IModel<Map> variablesModel)
	{
		return new TextTemplateHeaderContributor(new JavaScriptTemplate(template), variablesModel);
	}

	/**
	 * Construct.
	 * 
	 * @param template
	 *            The template with the contribution
	 * @param variablesModel
	 *            Optional model for variable substitution
	 */
	protected TextTemplateHeaderContributor(TextTemplate template, IModel<Map> variablesModel)
	{
		super(new TemplateModel(template, variablesModel));
	}
}