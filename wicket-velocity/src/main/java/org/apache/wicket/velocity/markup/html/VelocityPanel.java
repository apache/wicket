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
package org.apache.wicket.velocity.markup.html;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.resource.ResourceUtil;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.IStringResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;

/**
 * Panel that displays the result of rendering a <a
 * href="http://jakarta.apache.org/velocity">Velocity</a> template. The template itself can be any
 * {@link StringResourceStream} implementation, of which there are a number of convenient
 * implementations in the {@link org.apache.wicket.util} package. The model can be any normal
 * {@link Map}, which will be used to create the {@link VelocityContext}.
 * <p>
 * <b>Note:</b> Be sure to properly initialize the Velocity engine before using
 * {@link VelocityPanel }.
 * </p>
 */
public abstract class VelocityPanel extends Panel
	implements
		IMarkupResourceStreamProvider,
		IMarkupCacheKeyProvider
{
	private static final long serialVersionUID = 1L;

	/**
	 * Convenience factory method to create a {@link VelocityPanel} instance with a given
	 * {@link IStringResourceStream template resource}.
	 * 
	 * @param id
	 *            Component id
	 * @param model
	 *            optional model for variable substitution.
	 * @param templateResource
	 *            The template resource
	 * @return an instance of {@link VelocityPanel}
	 */
	@SuppressWarnings("rawtypes")
	public static VelocityPanel forTemplateResource(final String id,
		final IModel<? extends Map> model, final IResourceStream templateResource)
	{
		if (templateResource == null)
		{
			throw new IllegalArgumentException("argument templateResource must be not null");
		}

		return new VelocityPanel(id, model)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected IResourceStream getTemplateResource()
			{
				return templateResource;
			}
		};
	}

	private transient String stackTraceAsString;

	private transient String evaluatedTemplate;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            Component id
	 * @param templateResource
	 *            The velocity template as a string resource
	 * @param model
	 *            Model with variables that can be substituted by Velocity.
	 */
	@SuppressWarnings("rawtypes")
	public VelocityPanel(final String id, final IModel<? extends Map> model)
	{
		super(id, model);
	}

	/**
	 * Gets a reader for the velocity template.
	 * 
	 * @return reader for the velocity template
	 */
	private Reader getTemplateReader()
	{
		final IResourceStream resource = getTemplateResource();
		if (resource == null)
		{
			throw new IllegalArgumentException("getTemplateResource must return a resource");
		}

		final String template = ResourceUtil.readString(resource);
		if (template != null)
		{
			return new StringReader(template);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		if (!Strings.isEmpty(stackTraceAsString))
		{
			// TODO: only display the velocity error/stacktrace in development mode?
			replaceComponentTagBody(markupStream, openTag,
				Strings.toMultilineMarkup(stackTraceAsString));
		}
		else if (!parseGeneratedMarkup())
		{
			// check that no components have been added in case the generated
			// markup should not be parsed
			if (size() > 0)
			{
				throw new WicketRuntimeException(
					"Components cannot be added if the generated markup should not be parsed.");
			}

			if (evaluatedTemplate == null)
			{
				// initialize evaluatedTemplate
				getMarkupResourceStream(null, null);
			}
			replaceComponentTagBody(markupStream, openTag, evaluatedTemplate);
		}
		else
		{
			super.onComponentTagBody(markupStream, openTag);
		}
	}

	/**
	 * Either print or rethrow the throwable.
	 * 
	 * @param exception
	 *            the cause
	 * @param markupStream
	 *            the markup stream
	 * @param openTag
	 *            the open tag
	 */
	private void onException(final Exception exception)
	{
		if (!throwVelocityExceptions())
		{
			// print the exception on the panel
			stackTraceAsString = Strings.toString(exception);
		}
		else
		{
			// rethrow the exception
			throw new WicketRuntimeException(exception);
		}
	}

	/**
	 * Gets whether to escape HTML characters.
	 * 
	 * @return whether to escape HTML characters. The default value is false.
	 */
	protected boolean escapeHtml()
	{
		return false;
	}

	/**
	 * Returns the template resource passed to the constructor.
	 * 
	 * @return The template resource
	 */
	protected abstract IResourceStream getTemplateResource();

	/**
	 * Evaluates the template and returns the result.
	 * 
	 * @param templateReader
	 *            used to read the template
	 * @return the result of evaluating the velocity template
	 */
	private String evaluateVelocityTemplate(final Reader templateReader)
	{
		if (evaluatedTemplate == null)
		{
			// Get model as a map
			@SuppressWarnings("rawtypes")
			final Map map = (Map)getDefaultModelObject();

			// create a Velocity context object using the model if set
			final VelocityContext ctx = new VelocityContext(map);

			// create a writer for capturing the Velocity output
			StringWriter writer = new StringWriter();

			// string to be used as the template name for log messages in case
			// of error
			final String logTag = getId();
			try
			{
				// execute the velocity script and capture the output in writer
				Velocity.evaluate(ctx, writer, logTag, templateReader);

				// replace the tag's body the Velocity output
				evaluatedTemplate = writer.toString();

				if (escapeHtml())
				{
					// encode the result in order to get valid html output that
					// does not break the rest of the page
					evaluatedTemplate = Strings.escapeMarkup(evaluatedTemplate).toString();
				}
				return evaluatedTemplate;
			}
			catch (Exception e)
			{
				onException(e);
			}
			return null;
		}
		return evaluatedTemplate;
	}

	/**
	 * Gets whether to parse the resulting Wicket markup.
	 * 
	 * @return whether to parse the resulting Wicket markup. The default is false.
	 */
	protected boolean parseGeneratedMarkup()
	{
		return false;
	}

	/**
	 * Whether any velocity exception should be trapped and displayed on the panel (false) or thrown
	 * up to be handled by the exception mechanism of Wicket (true). The default is false, which
	 * traps and displays any exception without having consequences for the other components on the
	 * page.
	 * <p>
	 * Trapping these exceptions without disturbing the other components is especially useful in CMS
	 * like applications, where 'normal' users are allowed to do basic scripting. On errors, you
	 * want them to be able to have them correct them while the rest of the application keeps on
	 * working.
	 * 
	 * @return Whether any velocity exceptions should be thrown or trapped. The default is false.
	 */
	protected boolean throwVelocityExceptions()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final IResourceStream getMarkupResourceStream(final MarkupContainer container,
		final Class<?> containerClass)
	{
		Reader reader = getTemplateReader();
		if (reader == null)
		{
			throw new WicketRuntimeException("could not find velocity template for panel: " + this);
		}

		// evaluate the template and return a new StringResourceStream
		StringBuilder sb = new StringBuilder();
		sb.append("<wicket:panel>");
		sb.append(evaluateVelocityTemplate(reader));
		sb.append("</wicket:panel>");
		return new StringResourceStream(sb.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getCacheKey(final MarkupContainer container, final Class<?> containerClass)
	{
		// don't cache the evaluated template
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDetach()
	{
		super.onDetach();
		stackTraceAsString = null;
		evaluatedTemplate = null;
	}
}
